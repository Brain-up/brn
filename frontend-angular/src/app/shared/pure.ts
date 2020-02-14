export function showHappySnackbar(snackbar: { open: (...args: any[]) => void }, message: string) {
  snackbar.open(message, ' 😊 ', {duration: 2000});
  console.log(snackbar)
}

export function showSadSnackbar(snackbar: { open: (...args: any[]) => void }, err: { message: string }) {
  snackbar.open(err.message, ' 😪 ', {duration: 2000});
}
