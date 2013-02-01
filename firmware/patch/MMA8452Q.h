#ifndef _MMA8452Q_
#define _MMA8452Q_

#define STATUS 0x00

#define OUT_X_MSB 0x01
#define OUT_X_LSB 0x02

#define OUT_Y_MSB 0x03
#define OUT_Y_LSB 0x04

#define OUT_Z_MSB 0x05
#define OUT_Z_LSB 0x06

#define SYSMOD 0x0B
#define INT_SOURCE 0x0C
#define WHO_AM_I 0x0D
#define XYZ_DATA_CFG 0x0E
	#define HPF_OUT 4
	#define FS1 1
	#define FS0 0
#define HP_FILTER_CUTOFF 0x0F
	#define Pulse_HPF_BYP 5
	#define Pulse_LPF_EN 4
	#define SEL1 1
	#define SEL0 0

#define PL_STATUS 0x10
#define PL_CFG 0x11
#define PL_COUNT 0x12
#define PL_BF_ZCOMP 0x13
#define P_L_THS_REG 0x14

#define FF_MT_CFG 0x15
	#define FF_MT_ELE 7
	#define OAE 6
	#define ZEFE 5
	#define YEFE 4
	#define XEFE 3
#define FF_MT_SRC 0x16
	#define FF_MT_EA 7
	#define ZHE 5
	#define ZHP 4
	#define YHE 3
	#define XHE 1
	#define XHP 0
#define FF_MT_THS 0x17
	#define DBCNTM 7
	#define THS 0
#define FF_MT_COUNT 0x18

#define TRANSIENT_CFG 0x1D
	#define T_ELE 4
	#define ZTEFE 3
	#define YTEFE 2
	#define XTEFE 1
	#define HPF_BYP 0
#define TRANSIENT_SRC 0x1E
	#define T_EA 6
	#define ZTRANSE 5
	#define Z_Trans_Pol 4
	#define YTRANSE 3
	#define Y_Trans_Pol 2
	#define XTRANSE 1
	#define X_Trans_Pol 0
#define TRANSIENT_THS 0x1F
	//Same as FF_MT_THS
#define TRANSIENT_COUNT 0x20

#define PULSE_CFG 0x21
#define PULSE_SRC 0x22
#define PULSE_THSX 0x23
#define PULSE_THSY 0x24
#define PULSE_THSZ 0x25
#define PULSE_TMLT 0x26
#define PULSE_LTCY 0x27
#define PULSE_WIND 0x28

#define ASLP_COUNT 0x29

#define CTRL_REG1 0x2A
	#define ASLP_RATE1 7
	#define ASLP_RATE0 6
	#define DR2 5
	#define DR1 4
	#define DR0 3
	#define LNOISE 2
	#define F_READ 1
	#define ACTIVE 0
#define CTRL_REG2 0x2B
	#define ST 7
	#define RST 6
	#define SMODS1 4
	#define SMODS2 3
	#define SLPE 2
	#define MODS1 1
	#define MODS0 0
#define CTRL_REG3 0x2C
	#define WAKE_TRANS 6
	#define WAKE_LND_PRT 5
	#define WAKE_PULSE 4
	#define WAKE_FF_MT 3
	#define IPOL 1
	#define PP_OD 0
#define CTRL_REG4 0x2D
	#define INT_EN_ASLP 7
	#define INT_EN_FIFO 6
	#define INT_EN_TRANS 5
	#define INT_EN_LNDPRT 4
	#define INT_EN_PULSE 3
	#define INT_EN_FF_MT 2
	#define INT_EN_DRDY 0
#define CTRL_REG5 0x2E
	#define INT_CFG_ASLP 7
	#define INT_CFG_FIFO 6
	#define INT_CFG_TRANS 5
	#define INT_CFG_LNDPRT 4
	#define INT_CFG_PULSE 3
	#define INT_CFG_FF_MT 2
	#define INT_CFG_DRDY 0

#define OFF_X 0x2F
#define OFF_Y 0x30
#define OFF_Z 0x31

#endif
