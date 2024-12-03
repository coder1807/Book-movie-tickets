let selectedSeats = [];
let totalPrice = 0;
var checkoutButton = document.querySelector("#checkout-button");
window.onload = function () {
    updateCheckoutButton();
    var pTags = document.querySelectorAll(".chonViTri");
    var getLinks = document.querySelectorAll(".seatPlanButton");
    for (var i = 0; i < pTags.length; i++) {
        pTags[i].addEventListener('click', function () {
            var link = this.getAttribute('data-id');
            localStorage.setItem("data-id", this.getAttribute('data-id')); // Lưu id vào localStorage
            getLinks.forEach(getLink => {
                getLink.addEventListener('click', function (event) {
                    event.preventDefault();
                    if (getLink.dataset.type === 'student') {
                        fetch("/check-age")
                            .then(response => response.json())
                            .then(data => {
                                if (data.isValidAge) {
                                    if (!data.isVerified) {
                                        if (confirm("Tài khoản của bạn chưa được xác minh thẻ sinh viên. Bạn có muốn tiếp tục không ?")) {
                                            window.location.href = '/profile/detectStudent/' + data.userId;
                                        }
                                    } else {
                                        window.location.href = '/seats/schedules/' + link + '?is_student=true';
                                    }
                                } else {
                                    hienThiThongBao("Bạn không được phép chọn loại ghế này !", 5000, 'bg-danger');
                                }
                            })
                    } else {
                        window.location.href = '/seats/schedules/' + link;
                    }
                })
            })
        });
    }

    var seatImages = document.getElementsByClassName('seat-img');

    for (var i = 0; i < seatImages.length; i++) {
        // Lưu src gốc vào data-src-original và thiết lập trạng thái ban đầu là không được chọn
        seatImages[i].setAttribute('data-src-original', seatImages[i].src);
        seatImages[i].setAttribute('data-selected', 'false');

        seatImages[i].addEventListener('mouseover', function () {
            this.src = this.getAttribute('data-src2');
        });

        seatImages[i].addEventListener('mouseout', function () {
            if (this.getAttribute('data-selected') === 'false') {
                this.src = this.getAttribute('data-src-original');
            }
        });

        seatImages[i].addEventListener('click', function () {
            const seatId = this.getAttribute('data-id');
            const seatSymbol = this.getAttribute('data-symbol');
            const seatPrice = parseInt(this.getAttribute('data-price'));

            if (this.getAttribute('data-selected') === 'false') {
                // Check if the user already selected 7 seats
                if (selectedSeats.length >= 7) {
                    // Show error message and reset selections
                    document.querySelector('.error-message').textContent = 'Bạn chỉ có thể đặt tối đa 7 ghế 1 lần.';
                    document.querySelector('.error-message').style.display = 'block';
                    hienThiThongBao("Bạn chỉ được chọn tối đa 7 ghế 1 lần", 3000, 'bg-danger');

                    // Reset all selected seats
                    selectedSeats.forEach(seat => {
                        const seatElement = document.querySelector(`img[data-id="${seat.id}"]`);
                        seatElement.setAttribute('data-selected', 'false');
                        seatElement.src = seatElement.getAttribute('data-src-original');
                    });
                    selectedSeats = [];
                    totalPrice = 0;
                } else {
                    // Select the seat
                    if (isStudent && selectedSeats.length >= 1) { // Sinh viên chỉ được đặt 1 ghế
                        hienThiThongBao("Sinh viên chỉ được phép đặt 1 ghế !", 2000, 'bg-danger');
                        return;
                    }
                    this.setAttribute('data-selected', 'true');
                    this.src = this.getAttribute('data-src2');
                    selectedSeats.push({id: seatId, symbol: seatSymbol, price: seatPrice});
                    totalPrice += seatPrice;

                    // Thời gian giữ vé
                    timeInSeconds = 5 * 60;
                    localStorage.setItem("timeInSeconds", timeInSeconds);
                    clearInterval(countdownInterval);
                    countdownInterval = setInterval(updateCountdown, 1000);

                    hienThiThongBao("Bạn đã chọn ghế thành công", 2000, 'bg-success');

                    // Hide error message if any
                    document.querySelector('.error-message').style.display = 'none';
                }
            } else {
                // Deselect the seat
                this.setAttribute('data-selected', 'false');
                this.src = this.getAttribute('data-src-original');
                selectedSeats = selectedSeats.filter(seat => seat.id !== seatId);
                totalPrice -= seatPrice;

                // Nếu không còn ghế nào được chọn, dừng đếm ngược và xóa thời gian lưu
                clearInterval(countdownInterval);
                localStorage.removeItem("timeInSeconds");

                // Hide error message if any
                document.querySelector('.error-message').style.display = 'none';
            }

            updateCheckoutButton();

            // Update the UI
            updateUI();
        });
    }

    function updateUI() {
        const selectedSeatSymbols = selectedSeats.map(seat => seat.symbol).join(', ');
        document.querySelector('.selected-seats').textContent = selectedSeatSymbols;
        document.querySelector('.total-price').textContent = totalPrice.toLocaleString('vi-VN', {
            style: 'currency',
            currency: 'VND',
        });
    }

    if (checkoutButton) {
        checkoutButton.addEventListener('click', function () {
            const selectedSeatSymbols = selectedSeats.map(seat => ({
                id: seat.id,
                symbol: seat.symbol,
                price: seat.price
            }));
            document.getElementById('selectedSeatsInput').value = JSON.stringify(selectedSeatSymbols);
            document.getElementById('totalPriceInput').value = totalPrice;
            document.getElementById('checkout-form').submit();
        });
    }

//  document.querySelector(".tongTien").textContent = totalPrice.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });

    //function xử lí thông báo
    function hienThiThongBao(text, duration, className) {
        Toastify({
            text,
            duration,
            className,
            // destination: "https://github.com/apvarun/toastify-js",
            // newWindow: true, //giống target = blank
            close: false,
            gravity: "top",
            position: "left",
            stopOnFocus: true, // Prevents dismissing of toast on hover
            style: {
//                background: "linear-gradient(to right, #00b09b, #0f0)",

                width: 600,
                height: 250,
            },
            //  onClick: function () {}, // Callback after click
            backgroundColor: "red",
        }).showToast();
    }

    // Đặt thời gian bắt đầu là 5 phút (300 giây)
    let timeInSeconds = localStorage.getItem("timeInSeconds") ? parseInt(localStorage.getItem("timeInSeconds")) : 0;
    let countdownInterval; // lưu id của setInterval
    let timeout = false;

    // Nếu có thời gian được lưu và thời gian chưa hết, tiếp tục đếm ngược
    if (timeInSeconds > 0) {
        countdownInterval = setInterval(updateCountdown, 1000);
    }

    // Hàm để cập nhật thời gian mỗi giây
    function updateCountdown() {
        // Lấy phần tử hiển thị thời gian
        var timeElement = document.querySelector('.time');

        var minutes = Math.floor(timeInSeconds / 60);
        var seconds = timeInSeconds % 60;

        // Đảm bảo luôn hiển thị 2 chữ số cho phút và giây
        if (timeElement) {
            timeElement.textContent =
                (minutes < 10 ? '0' : '') + minutes + ':' +
                (seconds < 10 ? '0' : '') + seconds;
        }

        // Nếu thời gian còn lại lớn hơn 0, tiếp tục đếm ngược
        if (timeInSeconds > 0) {
            localStorage.setItem("timeInSeconds", timeInSeconds);
            timeInSeconds--;
        } else {
            // Thông báo khi hết thời gian và refresh
            if (!timeout) {
                hienThiThongBao("Đã hết thời gian giữ vé", 3000, 'bg-warning');
                timeout = true;

                setTimeout(() => {
                    if (localStorage.getItem("data-id")) {
                        const id = parseInt(localStorage.getItem("data-id"));
                        window.location.href = "/seats/schedules/" + id;
                    }
                }, 3000)

                // Dừng bộ đếm và xóa thời gian lưu
                clearInterval(countdownInterval);
                localStorage.removeItem("timeInSeconds"); // Xóa timeInSeconds khi hết thời gian
            }

        }
    }

    function updateCheckoutButton() {
        // Kiểm tra nếu tồn tại nút thanh toán và đã chọn ít nhất 1 ghế
        if (checkoutButton) {
            if (selectedSeats.length > 0) {
                checkoutButton.style.display = 'block'; // Hiển thị nút checkout
            } else {
                checkoutButton.style.display = 'none'; // Ẩn nút checkout
            }
        }
    }
}

