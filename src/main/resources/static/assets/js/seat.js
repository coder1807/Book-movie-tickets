let selectedSeats = [];
let totalPrice = 0;
window.onload = function () {
    var pTags = document.querySelectorAll(".chonViTri");
    var getLinks = document.querySelectorAll(".seatPlanButton");
    for (var i = 0; i < pTags.length; i++) {
        pTags[i].addEventListener('click', function () {
            var link = this.getAttribute('data-id');

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

        seatImages[i].addEventListener('dblclick', function () {
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

                // Hide error message if any
                document.querySelector('.error-message').style.display = 'none';
            }

            // Update the UI
            updateUI();
        });
    }

    function updateUI() {
        const selectedSeatSymbols = selectedSeats.map(seat => seat.symbol).join(', ');
        document.querySelector('.selected-seats').textContent = selectedSeatSymbols;
        document.querySelector('.total-price').textContent = totalPrice.toLocaleString('vi-VN', {
            style: 'currency',
            currency: 'VND'
        });
    }

    document.querySelector('#checkout-button').addEventListener('click', function () {
        const selectedSeatSymbols = selectedSeats.map(seat => ({
            id: seat.id,
            symbol: seat.symbol,
            price: seat.price
        }));
        document.getElementById('selectedSeatsInput').value = JSON.stringify(selectedSeatSymbols);
        document.getElementById('totalPriceInput').value = totalPrice;
        document.getElementById('checkout-form').submit();
    });

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


}


