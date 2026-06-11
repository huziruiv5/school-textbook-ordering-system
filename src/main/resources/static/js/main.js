// 学校教材订购系统 - 主JavaScript文件

document.addEventListener('DOMContentLoaded', function() {
    // 自动关闭提示消息
    setTimeout(function() {
        document.querySelectorAll('.alert-dismissible').forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
});
