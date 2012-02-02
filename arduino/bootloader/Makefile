# Author: Severin Smith

#Make sure you set your programmer name and port correctly

PROGRAMMER = -c avrispmkii -P usb
AVRDUDE = avrdude $(PROGRAMMER) -p $(DEVICE)

##Targets##

all: propinq_patch

propinq_patch: DEVICE     = atmega328p
propinq_patch: CLOCK      = 8000000
propinq_patch: FUSES      =  -U lfuse:w:0xC2:m -U hfuse:w:0xDA:m -U efuse:w:0x05:m
propinq_patch:
	$(AVRDUDE) $(FUSES)
	$(AVRDUDE) -U flash:w:ATmegaBOOT_168_atmega328_pro_8MHz.hex:i
