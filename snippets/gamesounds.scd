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

 

x = Synth(\whoosh);
x.set(\t_trig, 1); // run this line multiple times, to get multiple (very similar) bubbles!

y = Synth(\bubblebub);
y.set(\t_trig, 1); // run this line multiple times, to get multiple (very similar) bubbles!


x.free;
y.trig;