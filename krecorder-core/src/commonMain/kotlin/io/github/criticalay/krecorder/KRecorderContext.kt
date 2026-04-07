package io.github.criticalay.krecorder

/**
 * Platform context initialization.
 *
 * On Android, call [initKRecorder] with your Application or Activity context
 * before creating any [KRecorder] instances. On iOS this is a no-op.
 */
expect fun initKRecorder(context: Any)
