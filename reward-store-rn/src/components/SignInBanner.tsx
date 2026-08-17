import { Pressable, Text, View } from 'react-native';
import type { Styles } from '../styles/createStyles';

export function SignInBanner({
  styles,
  onPress,
}: {
  styles: Styles;
  onPress: () => void;
}) {
  return (
    <View style={styles.signInBanner}>
      <Text style={styles.signInBannerText}>
        로그인이 필요해요 · 리워드 교환은 로그인 후 이용할 수 있어요
      </Text>
      <Pressable style={styles.signInBannerButton} onPress={onPress}>
        <Text style={styles.signInBannerButtonText}>로그인하기</Text>
      </Pressable>
    </View>
  );
}
