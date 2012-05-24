/* Taken from http://en.wikibooks.org/wiki/Designing_Sound_in_SuperCollider/Bubbles */

(
SynthDef(\bubblebub, {  |out=0, t_trig=0, attack=0.01, decay=0.08, pitchcurvelen=0.1, freq=1000, doneAction=0, amp=0.1|
        var pitch, son;
        amp   = amp * EnvGen.ar(Env.perc(attack, decay).delay(0.003), t_trig, doneAction: doneAction);
        pitch = freq * EnvGen.ar(Env.new([0,0,1],[0,1]).exprange(1, 2.718), t_trig, timeScale: pitchcurvelen);
        son = SinOsc.ar(pitch);
        // high-pass to remove any lowpitched artifacts, scale amplitude
        son = HPF.ar(son, 500) * amp * 10;
        Out.ar(out, freq);
}).store
)
 
x = Synth(\bubblebub);
x.set(\freq, 350);
x.set(\t_trig, 1); // run this line multiple times, to get multiple (very similar) bubbles!
x.free;