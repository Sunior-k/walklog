import { ActivityIndicator, Modal, Pressable, Text, TextInput } from 'react-native';
import type { Styles } from '../styles/createStyles';

export function PromoCodeModal({
  styles,
  visible,
  code,
  isRedeeming,
  onChangeCode,
  onDismiss,
  onSubmit,
}: {
  styles: Styles;
  visible: boolean;
  code: string;
  isRedeeming: boolean;
  onChangeCode: (value: string) => void;
  onDismiss: () => void;
  onSubmit: () => void;
}) {
  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onDismiss}
    >
      <Pressable style={styles.modalOverlay} onPress={onDismiss}>
        <Pressable style={styles.modalCard} onPress={() => {}}>
          <Text style={styles.modalLabel}>이벤트/프로모션 코드 등록</Text>
          <TextInput
            style={styles.promoCodeInput}
            value={code}
            onChangeText={onChangeCode}
            placeholder="코드를 입력하세요"
            placeholderTextColor={styles.promoCodeInputPlaceholder.color}
            autoCapitalize="characters"
            autoCorrect={false}
            editable={!isRedeeming}
          />
          <Pressable
            style={[
              styles.modalPrimaryButton,
              code.trim().length === 0 && styles.redeemButtonDisabled,
            ]}
            onPress={onSubmit}
            disabled={isRedeeming || code.trim().length === 0}
          >
            {isRedeeming ? (
              <ActivityIndicator color={styles.onPrimaryColor.color} />
            ) : (
              <Text style={styles.modalPrimaryButtonText}>등록하기</Text>
            )}
          </Pressable>
          <Pressable style={styles.modalCloseButton} onPress={onDismiss}>
            <Text style={styles.modalCloseButtonText}>닫기</Text>
          </Pressable>
        </Pressable>
      </Pressable>
    </Modal>
  );
}
