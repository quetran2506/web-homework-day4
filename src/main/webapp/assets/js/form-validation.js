(() => {
    "use strict";
    document.querySelectorAll("form.needs-validation").forEach((form) => {
        const syncConfirm = () => {
            const confirm = form.querySelector("[data-match-password]");
            if (!confirm) return;
            const password = form.querySelector(`#${confirm.dataset.matchPassword}`);
            if (password) confirm.setCustomValidity(password.value === confirm.value ? "" : "Mật khẩu xác nhận chưa khớp.");
        };
        const syncFiles = () => {
            form.querySelectorAll("input[type=file][data-max-bytes]").forEach((input) => {
                const max = Number(input.dataset.maxBytes);
                const file = input.files && input.files[0];
                input.setCustomValidity(file && file.size > max ? `Tệp vượt quá giới hạn ${Math.floor(max / 1024 / 1024)} MB.` : "");
            });
        };
        form.addEventListener("input", syncConfirm);
        form.addEventListener("change", () => { syncConfirm(); syncFiles(); });
        form.addEventListener("submit", (event) => {
            syncConfirm();
            syncFiles();
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add("was-validated");
        });
    });
})();