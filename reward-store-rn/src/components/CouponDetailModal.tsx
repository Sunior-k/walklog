import { ActivityIndicator, Modal, Pressable, Text } from 'react-native';
import type { IssuedCoupon } from '../nativeModules/RewardBridge';
import type { Styles } from '../styles/createStyles';

export function CouponDetailModal({
  styles,
  coupon,
  isMarkingUsed,
  onDismiss,
  onMarkUsed,
}: {
  styles: Styles;
  coupon: IssuedCoupon | null;
  isMarkingUsed: boolean;
  onDismiss: () => void;
  onMarkUsed: () => void;
}) {
  return (
    <Modal
      visible={coupon !== null}
      transparent
      animationType="fade"
      onRequestClose={onDismiss}
    >
      <Pressable style={styles.modalOverlay} onPress={onDismiss}>
        <Pressable style={styles.modalCard} onPress={() => {}}>
          <Text style={styles.modalLabel}>쿠폰 코드</Text>
          <Text style={styles.modalCode}>{coupon?.code}</Text>
          {coupon?.status === 'ISSUED' ? (
            <Pressable
              style={styles.modalPrimaryButton}
              onPress={onMarkUsed}
              disabled={isMarkingUsed}
            >
              {isMarkingUsed ? (
                <ActivityIndicator color={styles.onPrimaryColor.color} />
              ) : (
                <Text style={styles.modalPrimaryButtonText}>
                  사용 완료로 표시
                </Text>
              )}
            </Pressable>
          ) : (
            <Text style={styles.modalUsedLabel}>이미 사용된 쿠폰이에요</Text>
          )}
          <Pressable style={styles.modalCloseButton} onPress={onDismiss}>
            <Text style={styles.modalCloseButtonText}>닫기</Text>
          </Pressable>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
