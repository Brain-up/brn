import { modifier } from 'ember-modifier';

interface IFrequencyOption {
  signal: { duration: number; frequency: number };
  word: string;
}

function draw(canvas: HTMLCanvasElement, frequency: number, duration: number, amplitude: number, width: number, height: number, text: string) {
  canvas.width = width * window.devicePixelRatio;
  canvas.height = height * window.devicePixelRatio;
  const ctx = canvas.getContext('2d')!;

  canvas.style.width = width + 'px';
  canvas.style.height = height + 'px';

  ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

  let startTime: number;
  let cancelFrame: number;

  const render = () => {
    ctx.fillStyle = 'white';

    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.font = '10px Arial';
    ctx.fillStyle = 'grey';
    ctx.textAlign = 'center';
    ctx.fillText(text, 100, window.devicePixelRatio * 6, 300);

    ctx.lineWidth = 1;

    const tick = Date.now();
    const timePassed = tick - startTime;
    const progress = timePassed / duration;
    const fullWidth = canvas.width / window.devicePixelRatio;
    const start = Math.round(progress * fullWidth);
    const center = canvas.height / window.devicePixelRatio / 2;
    const waveWidth = duration / 100;
    const end = start + waveWidth;
    const leftPad = end - fullWidth;

    const speed = 0;
    ctx.beginPath();

    for (let i = start; i <= Math.min(end, fullWidth); i += 1) {
      const isStart = i === start;
      const isEnd = i === Math.min(end, fullWidth);
      if (isStart) {
        ctx.moveTo(i, center);
      }
      ctx.lineTo(
        i,
        center + Math.sin(tick * speed + i * frequency) * amplitude,
      );
      if (isEnd) {
        ctx.lineTo(i, center);
      }
    }
    ctx.strokeStyle = `rgb(129, 213, 249)`;

    ctx.stroke();

    if (leftPad > 0) {
      ctx.beginPath();
      for (let i = 0; i <= leftPad; i += 1) {
        const isEnd = i === leftPad;
        ctx.lineTo(
          i,
          center + Math.sin(tick * speed + i * frequency) * amplitude,
        );
        if (isEnd) {
          ctx.lineTo(i, center);
        }
      }
      ctx.strokeStyle = `rgb(129, 213, 249)`;
      ctx.stroke();
    }

    ctx.beginPath();
    for (let i = leftPad > 0 ? leftPad : 0; i <= start; i += 1) {
      ctx.lineTo(i, center);
    }
    ctx.strokeStyle = `gray`;
    ctx.stroke();

    ctx.beginPath();
    for (let i = end; i <= canvas.width; i += 1) {
      ctx.lineTo(i, center);
    }
    ctx.strokeStyle = `gray`;
    ctx.stroke();

    if (timePassed < duration) {
      cancelFrame = requestAnimationFrame(render);
    } else {
      startTime = Date.now();
      cancelFrame = requestAnimationFrame(render);
    }
  };

  // Initialize animation
  startTime = Date.now();
  cancelFrame = requestAnimationFrame(render);

  return () => {
    cancelAnimationFrame(cancelFrame);
  };
}

export default modifier(function frequencyVisualizer(element: HTMLCanvasElement, [option]: [IFrequencyOption]) {
  const { duration, frequency } = option.signal;
  const parentNode = element.parentNode as HTMLElement;
  const cancel = draw(
    element,
    frequency * 1000,
    duration * 100,
    60,
    parentNode.clientWidth - 40,
    parentNode.clientHeight,
    option.word.split(':').pop()!,
  );

  return () => {
    cancel();
  };
});
