var selectAll = document.getElementById('checkboxAll');

function toggleCheckBoxes() {
	var checkboxes = document.querySelectorAll('input[type="checkbox"]');
	for (var i = 0; i < checkboxes.length; i++) {
		if (checkboxes[i] != this) {
			checkboxes[i].checked = this.checked;
		}
	}
}

selectAll.addEventListener("click", toggleCheckBoxes);
	