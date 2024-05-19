class AutoZoom {
	showPreviewImage = true;
	previewImageBPID = 0;
	cookieName = null;
	
	isTouchDevice = false;
	
	autoZoomToggle = null;
	previewImageDiv = null;
	previewImage = null;

	constructor(cookieName) {
		const that = this;
		this.cookieName = cookieName;
		this.isTouchDevice = 'ontouchstart' in document.documentElement;
		const cookie = $.cookie(this.cookieName);
		
		//An unset cookie should default to true.
		if(cookie == "false") {
			this.showPreviewImage = false;
		}
		else {
			this.showPreviewImage = true;
		}
		
		if(!this.isTouchDevice) {
			this._setupToggleButton();
		}

		this.previewImageDiv = $('<div>', {
			id: 'previewImage',
			class: 'previewImage',
			style: ""
		}).appendTo('body');
		this.previewImageDiv.append("<img></img>")
		this.previewImage = this.previewImageDiv.find("img")[0];
		
		this.previewImageDiv = this.previewImageDiv[0];		
	}
	
	_setupToggleButton() {
		const that = this;
		const enabledIcon = "ui-icon-search";
		const disabledIcon = "ui-icon-circle-close";
		
		const startingIcon = this.showPreviewImage ? enabledIcon : disabledIcon;
		
		this.autoZoomToggle = $("<button id='auto-zoom-toggle'>Auto-zoom cards on hover</button>").button(
		{
			icons:{
				primary:startingIcon
			}, 
			text:false
		});
		
		this.autoZoomToggle.click(
			function () {
				if (that.showPreviewImage) {
					that.autoZoomToggle.button("option", "icons", {primary:disabledIcon});
					that.showPreviewImage = false;
					that.saveCookieValue();
				} else {
					that.autoZoomToggle.button("option", "icons", {primary:enabledIcon});
					that.showPreviewImage = true;
					that.saveCookieValue();
				}
			});
		
		var selected = $("#previewImageOnHover").prop("checked");
	}
	
	saveCookieValue() {
		$.cookie(this.cookieName, "" + this.showPreviewImage, { expires: 365 });
	}
	
	// make the preview image shown be the reference image that's hovered on:
	displayPreviewImage(refImageDiv) {
	
		const that = this;
		
		this.previewImage.onload = function () {
			
			that.previewImage.style.display = "block";
			
			// get position and size of the reference image (actually the parent div):
			var rect = refImageDiv.getBoundingClientRect();
			var srcImageX = rect.left;
			var srcImageY = rect.top;
			var srcImageWidth = rect.right - rect.left;
			var srcImageHeight = rect.bottom - rect.top;
			// get the size of the browser window:
			var windowWidth = window.innerWidth;
			var windowHeight = window.innerHeight;
			// get the elements to be altered:
			var previewImageStyle = that.previewImageDiv.style;
			var previewImageImgStyle = that.previewImage.style;
			var previewImageHeight = that.previewImage.naturalHeight;
			var previewImageWidth = that.previewImage.naturalWidth;
			
			var ratio = previewImageWidth / previewImageHeight;

			if (previewImageHeight > windowHeight / 2) {
				previewImageHeight = windowHeight / 2;
				previewImageWidth = ratio * previewImageHeight;
			}
			else if (previewImageWidth > windowWidth / 2) {
				previewImageWidth = windowWidth / 2;
				previewImageHeight = previewImageWidth / ratio;
			}

			// set the horizontal position of the preview image:
			const rightEdge = srcImageX + srcImageWidth;
			const leftEdge = srcImageX;
			const goesPastRightBound = rightEdge + previewImageWidth > windowWidth;
			const goesPastLeftBound = leftEdge - previewImageWidth < 0;
			var previewImageLeft = rightEdge;
			
			if (goesPastRightBound && goesPastLeftBound) {
				// if previewImage would extend past either left or right side
				// of screen, display the previewImage in the biggest space 
				// available and shrink to fit
				const rightSpace = windowWidth - (leftEdge + srcImageWidth);
				const leftSpace = leftEdge;
				if (rightSpace > leftSpace) {
					previewImageWidth = rightSpace;
					previewImageLeft = rightEdge;
				}
				else {
					previewImageWidth = leftSpace;
					previewImageLeft = leftEdge - previewImageWidth;
				}
				previewImageHeight = previewImageWidth / ratio;
			}
			else {
				if (goesPastRightBound) {
					previewImageLeft = leftEdge - previewImageWidth;
				}
				else if (goesPastLeftBound) {
					previewImageLeft = rightEdge;
				}
			}

			// set the vertical position of the preview image (and make sure it isn't extending over the edge of the window):
			var previewImageTop = (srcImageY + (srcImageHeight / 2)) - (previewImageHeight / 2);
			if ((previewImageTop + previewImageHeight) > windowHeight) {
				previewImageTop = windowHeight - previewImageHeight;
			}
			else if (previewImageTop < 0) {
				previewImageTop = 0;
			}

			// assign the positions to the preview image element:
			previewImageStyle.left = previewImageLeft + "px";
			previewImageStyle.top = previewImageTop + "px";
			previewImageImgStyle.width = previewImageWidth + 'px';
			previewImageImgStyle.height = previewImageHeight + 'px';
		}
		
		let cardImage = Card.getImageUrl(this.previewImageBPID);

		if (cardImage != null) {
			this.previewImage.src = cardImage;
		}
	}

	hidePreviewImage() {
		this.previewImageBPID = "0";
		this.previewImage.src = "";
		this.previewImage.style.display = "none";
	}

	rotatePreviewImage(shouldRotate) {
		var previewImageStyle = this.previewImage.style;
		if (!shouldRotate) {
			previewImageStyle.transform = "rotate(0deg)";
		}
		else {
			previewImageStyle.transform = "rotate(180deg)";
		}
	}

	handleMouseOver(event, isDragging, infoDialogOpen) {
		const target = $(event.target);
		const tarIsCard = target.hasClass("actionArea");
		
		// if mouse over target is a card on table, and client supports image previews, showImage
		if(this.isTouchDevice || !this.showPreviewImage
		   || !tarIsCard || isDragging || infoDialogOpen) {
			
			// if previewImage is active and either the event target isn't a card 
			// on table OR the user shift+clicked to bring up the card detail 
			// dialogue, we need to hide the current previewImage
			if (this.previewImageBPID !== "0" && !tarIsCard) {
				this.hidePreviewImage();
				event.stopPropagation();
				return false;
			}
			
			return true;
		}

		
		const refCard = target.closest(".card");
		const refCardDiv = refCard[0];
		const card = refCard.data("card");
		
		// don't show preview image if card is animating
		if (!$(refCardDiv).hasClass('card-animating')) {
			const startFlipped = event.shiftKey;
			const blueprintId = card.blueprintId;
			const reverseSideImage = Card.getImageUrl(blueprintId + "_BACK");
			const imageBlueprintId = startFlipped && reverseSideImage ? blueprintId + "_BACK" : blueprintId;

			// don't show preview image if hovered card is the DS/LS card back art
			if (imageBlueprintId !== "-1_1" && imageBlueprintId !== "-1_2") {
				this.previewImageBPID = imageBlueprintId;
				this.displayPreviewImage(refCardDiv);

				if (!reverseSideImage) {
					// set the starting rotation based on if shift key
					// is active when event was triggered, as long as the
					// card doesn't have a reverse side
					this.rotatePreviewImage(startFlipped);
				}
				
				event.stopPropagation();
				return false;
			}
		}
	}
	
	handleMouseDown(event) {
		if (this.previewImageBPID !== 0) {
			this.hidePreviewImage();
		}
	}
	
	handleKeyDown(event) {
		if (this.showPreviewImage && !this.isTouchDevice 
				&& event.which === 16 && this.previewImageBPID != "0") {
			const reverseSideImage = Card.getImageUrl(this.previewImageBPID + "_BACK");
		
			if (reverseSideImage) {
				this.previewImageBPID = this.previewImageBPID + "_BACK";
				this.previewImage.src = reverseSideImage;
			} 
			else {
				this.rotatePreviewImage(true)
			}
		};
		return true;
	}
	
	handleKeyUp(event) {
		if (this.showPreviewImage && !this.isTouchDevice 
				&& event.which === 16 && this.previewImageBPID != "0") {
			const isBackImage = this.previewImageBPID.endsWith('_BACK');
			if (isBackImage) {
				this.previewImageBPID = this.previewImageBPID.substring(0, this.previewImageBPID.length - 5);
				this.previewImage.src = Card.getImageUrl(this.previewImageBPID);
			} else {
				this.rotatePreviewImage(false)
			}
		};
		return true;
	}
	
}