SynthDef(\whoosh, {  
	arg out=0, t_trig=0, amp=1, lp_gain=3.8, freq=75, doneAction=0;
	var pitch, sound;

	amp   = amp * EnvGen.ar(Env.new([0, 1, 0], [0.5, 0.01]), t_trig, doneAction: doneAction);
	pitch = freq * EnvGen.ar(Env.new([1, 10, 0], [0.5, 0.01]), t_trig, doneAction: doneAction);

	sound = amp * WhiteNoise.ar(1);
	sound = MoogFF.ar(sound, pitch, lp_gain);
	
	Out.ar(out, sound);
}).store;

SynthDef(\bubblebub, {
	arg out=0, t_trig=0, attack=0.01, decay=0.08, pitchcurvelen=0.1, freq=1000, doneAction=0, amp=0.1;
	var pitch, son;
	
	amp   = amp * EnvGen.ar(Env.perc(attack, decay).delay(0.003), t_trig, doneAction: doneAction);
	pitch = freq * EnvGen.ar(Env.new([0,0,1],[0,1]).exprange(1, 2.718), t_trig, timeScale: pitchcurvelen);
	
	son = SinOsc.ar(pitch);
	// high-pass to remove any lowpitched artifacts, scale amplitude
	son = HPF.ar(son, 500) * amp * 10;
	
	Out.ar(out, son);
}).store;

SynthDef(\whooshbubble, {
	arg 
		out=0, t_trig=0, doneAction=0, 
		b_attack=0.01, b_decay=0.08, b_pitchcurvelen=0.1, b_freq=400, b_amp=0.1,
		wamp=1, wlp_gain=3.8, wfreq=75;
	var wampenv, wbtrig, w_pitch, w_sound, b_pitch, b_sound;
	
		
	
	wampenv   =  EnvGen.ar(Env.new([0, 1, 0], [0.5, 0.01]), t_trig, doneAction: doneAction);
	wamp = wamp * wampenv;
	w_pitch = wfreq * EnvGen.ar(Env.new([1, 10, 0], [0.5, 0.01]), t_trig, doneAction: doneAction);

	w_sound = wamp * WhiteNoise.ar(1);
	w_sound = MoogFF.ar(w_sound, w_pitch, wlp_gain);
	
	wbtrig = Done.kr(wampenv);
	
	b_amp   = b_amp * EnvGen.ar(Env.perc(b_attack, b_decay).delay(0.003), wbtrig, doneAction: doneAction);
	b_pitch = b_freq * EnvGen.ar(Env.new([0,0,1],[0,1]).exprange(1, 2.718), wbtrig, timeScale: b_pitchcurvelen);
	
	b_sound = SinOsc.ar(b_pitch);
	// high-pass to remove any lowpitched artifacts, scale amplitude
	b_sound = HPF.ar(b_sound, 500) * b_amp * 10;


	Out.ar(out, w_sound+b_sound);
}).store;


x = Synth(\whoosh);
x.set(\t_trig, 1);

y = Synth(\bubblebub);
y.set(\t_trig, 1);

z = Synth(\whooshbubble);
z.set(\t_trig, 1);

z.free;
x.free;
y.trig;