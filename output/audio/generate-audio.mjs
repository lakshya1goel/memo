// Original synthesized game audio. Run with Node.js; no packages are needed.
// Uses exclusive file writes to avoid overwriting any existing assets.
import fs from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
const resourceDir = fileURLToPath(new URL('../../app/src/main/res/raw/', import.meta.url));
const sampleRate = 44100;
const tau = 2 * Math.PI;
const beat = 60 / 96;
const frequency = midi => 440 * 2 ** ((midi - 69) / 12);
const buffer = seconds => new Float64Array(Math.round(seconds * sampleRate));

function note(out, start, duration, midi, gain, voice = 'bell', loop = false) {
    const first = Math.round(start * sampleRate);
    const frames = Math.round(duration * sampleRate);
    const hz = frequency(midi);
    for (let i = 0; i < frames; i++) {
        const t = i / sampleRate;
        const progress = i / (frames - 1);
        const attack = Math.min(1, t / (voice === 'pad' ? 0.12 : 0.008));
        const release = Math.min(1, (1 - progress) * duration / 0.09);
        const envelope = attack * release;
        let tone;
        if (voice === 'pad') {
            tone = (Math.sin(tau * hz * t) + 0.10 * Math.sin(tau * hz * 2 * t))
                * Math.sin(Math.PI * progress) ** 0.6;
        } else if (voice === 'wood') {
            tone = (Math.sin(tau * hz * t) * Math.exp(-t * 15)
                + 0.20 * Math.sin(tau * hz * 2 * t) * Math.exp(-t * 35));
        } else {
            tone = Math.sin(tau * hz * t) * Math.exp(-t * 4.5)
                + 0.16 * Math.sin(tau * hz * 2 * t) * Math.exp(-t * 8)
                + 0.05 * Math.sin(tau * hz * 3 * t) * Math.exp(-t * 12);
        }
        const at = loop ? (first + i) % out.length : first + i;
        if (at < out.length) out[at] += gain * envelope * tone;
    }
}

async function save(name, signal, targetPeak) {
    let peak = 0;
    for (const value of signal) {
        if (!Number.isFinite(value)) throw new Error('Non-finite sample');
        peak = Math.max(peak, Math.abs(value));
    }
    if (peak === 0) throw new Error('Silent asset');
    const scale = targetPeak / peak;
    const wav = Buffer.alloc(44 + signal.length * 2);
    wav.write('RIFF', 0);
    wav.writeUInt32LE(wav.length - 8, 4);
    wav.write('WAVE', 8);
    wav.write('fmt ', 12);
    wav.writeUInt32LE(16, 16);
    wav.writeUInt16LE(1, 20); // PCM
    wav.writeUInt16LE(1, 22); // mono
    wav.writeUInt32LE(sampleRate, 24);
    wav.writeUInt32LE(sampleRate * 2, 28);
    wav.writeUInt16LE(2, 32);
    wav.writeUInt16LE(16, 34);
    wav.write('data', 36);
    wav.writeUInt32LE(signal.length * 2, 40);
    let sumSquares = 0;
    let largestStep = 0;
    for (let i = 0; i < signal.length; i++) {
        const value = signal[i] * scale;
        if (Math.abs(value) >= 1) throw new Error('Clipping');
        wav.writeInt16LE(Math.round(value * 32767), 44 + i * 2);
        sumSquares += value * value;
        if (i) largestStep = Math.max(largestStep, Math.abs(value - signal[i - 1] * scale));
    }
    await fs.writeFile(resourceDir + name + '.wav', wav, { flag: 'wx' });
    return {
        file: name + '.wav', seconds: signal.length / sampleRate,
        sampleRate, channels: 1, bitDepth: 16, bytes: wav.length,
        peak: targetPeak, rms: Math.sqrt(sumSquares / signal.length),
        boundaryStep: Math.abs(signal[0] - signal[signal.length - 1]) * scale,
        largestStep
    };
}

await fs.mkdir(resourceDir, { recursive: true });
const reports = [];
// Eight bars at 96 BPM: C, F, Am, G, C, F, G, C.
// Sustained and delayed tails wrap around so the phrase can repeat continuously.
const music = buffer(20);
const chords = [
    [48, 52, 55], [53, 57, 60], [45, 48, 52], [43, 47, 50],
    [48, 52, 55], [53, 57, 60], [43, 47, 50], [48, 52, 55]
];
const melody = [
    [72, 76, 79, 76], [77, 81, 79, 77], [76, 72, 69, 72], [71, 74, 79, 74],
    [76, 79, 84, 79], [81, 77, 76, 72], [74, 79, 77, 71], [72, 76, 79, 72]
];
chords.forEach((chord, bar) => {
    const start = bar * 4 * beat;
    chord.forEach(midi => note(music, start, 4 * beat + 0.15, midi, 0.035, 'pad', true));
    note(music, start, 1.1, chord[0] - 12, 0.045, 'bell', true);
    melody[bar].forEach((midi, step) => {
        note(music, start + step * beat, 0.95, midi, step === 0 ? 0.12 : 0.09, 'bell', true);
    });
});
const dryMusic = music.slice();
const delayFrames = Math.round(beat * 0.75 * sampleRate);
for (let i = 0; i < music.length; i++) {
    music[(i + delayFrames) % music.length] += dryMusic[i] * 0.13;
}
reports.push(await save('music_garden_loop', music, 0.28));

const tap = buffer(0.13);
note(tap, 0, 0.12, 81, 0.65, 'wood');
note(tap, 0, 0.08, 57, 0.20, 'wood');
reports.push(await save('sfx_card_tap', tap, 0.42));

const match = buffer(0.90);
[72, 76, 79, 84].forEach((midi, i) => note(match, i * 0.10, 0.55, midi, 0.34, 'bell'));
reports.push(await save('sfx_pair_match', match, 0.55));

const mismatch = buffer(0.65);
note(mismatch, 0, 0.28, 64, 0.5, 'bell');
note(mismatch, 0.14, 0.36, 60, 0.4, 'bell');
note(mismatch, 0.27, 0.34, 55, 0.3, 'bell');
reports.push(await save('sfx_pair_miss', mismatch, 0.38));

await fs.writeFile(new URL('audio-validation.json', import.meta.url),
    JSON.stringify({ provenance: 'Original procedural synthesis; no external samples.', reports }, null, 2) + '\n',
    { flag: 'wx' });
export { reports };
