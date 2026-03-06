import com.github.xujia118.common.dto.InventoryFailedEvent;
import com.github.xujia118.common.dto.OrderDto;
import com.github.xujia118.common.model.OrderStatus;
import com.github.xujia118.common.model.PaymentType;
import com.github.xujia118.paymentservice.model.Payment;
import com.github.xujia118.paymentservice.producer.PaymentPublisher;
import com.github.xujia118.paymentservice.repository.PaymentRepository;
import com.github.xujia118.paymentservice.service.PaymentProvider;
import com.github.xujia118.paymentservice.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProvider paymentProvider;

    @Mock
    private PaymentPublisher paymentPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private OrderDto testOrderDto;
    private Payment testPayment;
    private InventoryFailedEvent testInventoryFailedEvent;

    @BeforeEach
    void setUp() {
        testOrderDto = new OrderDto();
        testOrderDto.setOrderId(UUID.randomUUID().toString());
        testOrderDto.setAccountId("1");
        testOrderDto.setTotalAmount(new BigDecimal("100.00"));
        testOrderDto.setPaymentType(PaymentType.CREDIT_CARD);
        testOrderDto.setPaymentMethodId(1L);

        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setOrderId(testOrderDto.getOrderId());
        testPayment.setAccountId(testOrderDto.getAccountId());
        testPayment.setTotalAmount(testOrderDto.getTotalAmount());
        testPayment.setPaymentType(testOrderDto.getPaymentType());
        testPayment.setOrderStatus(OrderStatus.PENDING);

        testInventoryFailedEvent = new InventoryFailedEvent();
        testInventoryFailedEvent.setOrderId(testOrderDto.getOrderId());
        testInventoryFailedEvent.setTransactionId("txn-123");
    }

    @Test
    void processOrder_WithNewOrder_ShouldProcessSuccessfully() {
        when(paymentRepository.findByOrderId(testOrderDto.getOrderId())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentProvider.executeTransaction(any(Payment.class), anyLong())).thenReturn(true);

        paymentService.processOrder(testOrderDto);

        verify(paymentRepository, times(1)).findByOrderId(testOrderDto.getOrderId());
        verify(paymentRepository, times(2)).save(any(Payment.class)); // Initial save + success update
        verify(paymentProvider, times(1)).executeTransaction(any(Payment.class), anyLong());
        verify(paymentPublisher, times(1)).publishPaymentSuccess(eq(testOrderDto), any(Payment.class));
        verify(paymentPublisher, never()).publishPaymentFailure(any(OrderDto.class), any(Payment.class));
    }

    @Test
    void processOrder_WithExistingPayment_ShouldSkipProcessing() {
        when(paymentRepository.findByOrderId(testOrderDto.getOrderId())).thenReturn(Optional.of(testPayment));

        paymentService.processOrder(testOrderDto);

        verify(paymentRepository, times(1)).findByOrderId(testOrderDto.getOrderId());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentProvider, never()).executeTransaction(any(Payment.class), anyLong());
        verify(paymentPublisher, never()).publishPaymentSuccess(any(OrderDto.class), any(Payment.class));
        verify(paymentPublisher, never()).publishPaymentFailure(any(OrderDto.class), any(Payment.class));
    }

    @Test
    void processOrder_WithPaymentFailure_ShouldHandleFailure() {
        when(paymentRepository.findByOrderId(testOrderDto.getOrderId())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentProvider.executeTransaction(any(Payment.class), anyLong())).thenReturn(false);

        paymentService.processOrder(testOrderDto);

        verify(paymentRepository, times(1)).findByOrderId(testOrderDto.getOrderId());
        verify(paymentRepository, times(2)).save(any(Payment.class)); // Initial save + failure update
        verify(paymentProvider, times(1)).executeTransaction(any(Payment.class), anyLong());
        verify(paymentPublisher, never()).publishPaymentSuccess(any(OrderDto.class), any(Payment.class));
        verify(paymentPublisher, times(1)).publishPaymentFailure(eq(testOrderDto), any(Payment.class));
    }

    @Test
    void processRefund_WithValidPayment_ShouldProcessRefund() {
        when(paymentRepository.findByOrderId(testInventoryFailedEvent.getOrderId())).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        paymentService.processRefund(testInventoryFailedEvent);

        verify(paymentRepository, times(1)).findByOrderId(testInventoryFailedEvent.getOrderId());
        verify(paymentProvider, times(1)).refund(testInventoryFailedEvent.getTransactionId(), testPayment.getTotalAmount());
        verify(paymentRepository, times(1)).save(testPayment);
        assertEquals(OrderStatus.REFUNDED, testPayment.getOrderStatus());
    }

    @Test
    void processRefund_WithNonExistentPayment_ShouldThrowEntityNotFoundException() {
        when(paymentRepository.findByOrderId(testInventoryFailedEvent.getOrderId())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            paymentService.processRefund(testInventoryFailedEvent);
        });

        assertEquals("Payment not found for order: " + testInventoryFailedEvent.getOrderId(), exception.getMessage());
        verify(paymentRepository, times(1)).findByOrderId(testInventoryFailedEvent.getOrderId());
        verify(paymentProvider, never()).refund(anyString(), any(BigDecimal.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processRefund_WithAlreadyRefundedPayment_ShouldSkipRefund() {
        testPayment.setOrderStatus(OrderStatus.REFUNDED);
        when(paymentRepository.findByOrderId(testInventoryFailedEvent.getOrderId())).thenReturn(Optional.of(testPayment));

        paymentService.processRefund(testInventoryFailedEvent);

        verify(paymentRepository, times(1)).findByOrderId(testInventoryFailedEvent.getOrderId());
        verify(paymentProvider, never()).refund(anyString(), any(BigDecimal.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        assertEquals(OrderStatus.REFUNDED, testPayment.getOrderStatus());
    }

    @Test
    void createInitialPayment_ShouldCreatePaymentWithCorrectStatus() throws Exception {
        Method method = PaymentService.class.getDeclaredMethod("createInitialPayment", OrderDto.class);
        method.setAccessible(true);
        
        Payment result = (Payment) method.invoke(paymentService, testOrderDto);

        assertNotNull(result);
        assertEquals(testOrderDto.getOrderId(), result.getOrderId());
        assertEquals(testOrderDto.getAccountId(), result.getAccountId());
        assertEquals(testOrderDto.getTotalAmount(), result.getTotalAmount());
        assertEquals(testOrderDto.getPaymentType(), result.getPaymentType());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
    }

    @Test
    void handleSuccess_ShouldUpdateStatusAndPublishSuccess() throws Exception {
        Method method = PaymentService.class.getDeclaredMethod("handleSuccess", OrderDto.class, Payment.class);
        method.setAccessible(true);
        
        Payment payment = new Payment();
        payment.setOrderStatus(OrderStatus.PENDING);

        method.invoke(paymentService, testOrderDto, payment);

        assertEquals(OrderStatus.PAID, payment.getOrderStatus());
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentPublisher, times(1)).publishPaymentSuccess(testOrderDto, payment);
    }

    @Test
    void handleFailure_ShouldUpdateStatusAndPublishFailure() throws Exception {
        Method method = PaymentService.class.getDeclaredMethod("handleFailure", OrderDto.class, Payment.class);
        method.setAccessible(true);
        
        Payment payment = new Payment();
        payment.setOrderStatus(OrderStatus.PENDING);

        method.invoke(paymentService, testOrderDto, payment);

        assertEquals(OrderStatus.FAILED, payment.getOrderStatus());
        verify(paymentRepository, times(1)).save(payment);
        verify(paymentPublisher, times(1)).publishPaymentFailure(testOrderDto, payment);
    }
}
