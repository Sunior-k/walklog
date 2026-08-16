import { StyleSheet } from 'react-native';
import type { WalkLogPalette } from '../theme/walklogColors';

export type Styles = ReturnType<typeof createStyles>;

export function createStyles(palette: WalkLogPalette) {
  return StyleSheet.create({
    container: {
      flex: 1,
      backgroundColor: palette.background,
    },
    header: {
      paddingHorizontal: 20,
      paddingTop: 12,
      paddingBottom: 20,
    },
    headerRow: {
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
    headerTitle: {
      color: palette.textPrimary,
      fontSize: 22,
      fontWeight: '700',
    },
    promoCodeButton: {
      borderWidth: 1,
      borderColor: palette.primary,
      borderRadius: 50,
      paddingHorizontal: 12,
      paddingVertical: 6,
    },
    promoCodeButtonText: {
      color: palette.primary,
      fontSize: 12,
      fontWeight: '700',
    },
    balanceLabel: {
      marginTop: 6,
      color: palette.primary,
      fontSize: 16,
      fontWeight: '600',
    },
    signInBanner: {
      marginHorizontal: 20,
      marginBottom: 12,
      padding: 14,
      borderRadius: 14,
      backgroundColor: palette.surfaceVariant,
      borderWidth: 1,
      borderColor: palette.primary,
      gap: 10,
    },
    signInBannerText: {
      color: palette.textPrimary,
      fontSize: 13,
      lineHeight: 18,
    },
    signInBannerButton: {
      alignSelf: 'flex-start',
      backgroundColor: palette.primary,
      borderRadius: 50,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    signInBannerButtonText: {
      color: palette.onPrimary,
      fontSize: 12,
      fontWeight: '700',
    },
    listContent: {
      paddingHorizontal: 20,
      paddingBottom: 24,
      gap: 12,
    },
    card: {
      flexDirection: 'row',
      alignItems: 'center',
      backgroundColor: palette.surface,
      borderColor: palette.border,
      borderWidth: 1,
      borderRadius: 18,
      padding: 16,
      gap: 12,
    },
    cardEmoji: {
      fontSize: 28,
    },
    cardBody: {
      flex: 1,
    },
    cardTitle: {
      color: palette.textPrimary,
      fontSize: 15,
      fontWeight: '600',
    },
    cardDescription: {
      color: palette.textSecondary,
      fontSize: 12,
      marginTop: 2,
    },
    cardCost: {
      color: palette.primary,
      fontSize: 13,
      fontWeight: '600',
      marginTop: 6,
    },
    redeemButton: {
      backgroundColor: palette.primary,
      borderRadius: 50,
      paddingHorizontal: 14,
      paddingVertical: 8,
    },
    redeemButtonDisabled: {
      backgroundColor: palette.border,
    },
    redeemButtonText: {
      color: palette.onPrimary,
      fontSize: 12,
      fontWeight: '700',
    },
    couponSection: {
      marginTop: 20,
      borderTopWidth: 1,
      borderTopColor: palette.border,
      paddingTop: 16,
      gap: 4,
    },
    couponSectionTitle: {
      color: palette.textPrimary,
      fontSize: 14,
      fontWeight: '600',
      marginBottom: 10,
    },
    couponEmptyText: {
      color: palette.textSecondary,
      fontSize: 12,
      lineHeight: 18,
      paddingVertical: 4,
    },
    couponRow: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      paddingVertical: 10,
      paddingHorizontal: 12,
      borderRadius: 10,
      backgroundColor: palette.surface,
    },
    couponRowUsed: {
      opacity: 0.45,
    },
    couponCode: {
      color: palette.primary,
      fontSize: 13,
      fontWeight: '700',
      letterSpacing: 1,
    },
    couponStatus: {
      color: palette.textSecondary,
      fontSize: 12,
    },
    modalOverlay: {
      flex: 1,
      backgroundColor: 'rgba(0,0,0,0.55)',
      justifyContent: 'center',
      alignItems: 'center',
      padding: 24,
    },
    modalCard: {
      width: '100%',
      maxWidth: 320,
      backgroundColor: palette.surface,
      borderRadius: 20,
      padding: 24,
      alignItems: 'center',
      gap: 12,
    },
    modalLabel: {
      color: palette.textSecondary,
      fontSize: 12,
    },
    modalCode: {
      color: palette.primary,
      fontSize: 24,
      fontWeight: '700',
      letterSpacing: 2,
    },
    promoCodeInput: {
      alignSelf: 'stretch',
      color: palette.textPrimary,
      fontSize: 16,
      fontWeight: '600',
      letterSpacing: 1,
      textAlign: 'center',
      borderWidth: 1,
      borderColor: palette.border,
      borderRadius: 12,
      paddingVertical: 10,
      paddingHorizontal: 12,
    },
    promoCodeInputPlaceholder: {
      color: palette.textSecondary,
    },
    modalUsedLabel: {
      color: palette.textSecondary,
      fontSize: 13,
    },
    modalPrimaryButton: {
      marginTop: 8,
      alignSelf: 'stretch',
      backgroundColor: palette.primary,
      borderRadius: 50,
      paddingVertical: 12,
      alignItems: 'center',
    },
    modalPrimaryButtonText: {
      color: palette.onPrimary,
      fontSize: 14,
      fontWeight: '700',
    },
    modalCloseButton: {
      marginTop: 4,
      paddingVertical: 8,
    },
    modalCloseButtonText: {
      color: palette.textSecondary,
      fontSize: 13,
    },
    onPrimaryColor: {
      color: palette.onPrimary,
    },
  });
}
