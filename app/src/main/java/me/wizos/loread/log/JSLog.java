// package me.wizos.loread.log;
//
// import com.elvishew.xlog.XLog;
//
// public class JSLog {
//     private JSLog() {}
//     private static JSLog instance;
//     public static JSLog i() {
//         if (instance == null) {
//             synchronized (JSLog.class) {
//                 if (instance == null) {
//                     instance = new JSLog();
//                 }
//             }
//         }
//         return instance;
//     }
//
//     public void v(Object object){
//         XLog.v(object);
//     }
//     public void d(Object object){
//         XLog.d(object);
//     }
//     public void i(Object object){
//         XLog.i(object);
//     }
//     public void w(Object object){
//         XLog.w(object);
//     }
//     public void e(Object object){
//         XLog.e(object);
//     }
// }
