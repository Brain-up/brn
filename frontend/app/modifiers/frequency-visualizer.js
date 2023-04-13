import { modifier } from 'ember-modifier';

function draw(canvas, frequency, duration, amplitude, width, height, text) {
  canvas.width = width * window.devicePixelRatio;
  canvas.height = height * window.devicePixelRatio;
  const ctx = canvas.getContext('2d');

  canvas.style.width = width + 'px';
  canvas.style.height = height + 'px';

  ctx.scale(window.devicePixelRatio, window.devicePixelRatio);

  // const audioCtx = new AudioContext();
  // let oscillator;
  let startTime;
  let cancelFrame;

  const render = () => {
    ctx.fillStyle = 'white';

    // white text on the top of canvas

    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.font = '10px Arial';
    ctx.fillStyle = 'grey';
    ctx.textAlign = 'center';
    // fill text in the bottom of canvas
    ctx.fillText(text, 100, window.devicePixelRatio * 6, 300);

    // ctx.fillText(text, canvas.width / 2, canvas.height - 40, 300);

    ctx.lineWidth = 1;

    // Draw horizontal wave for frequency representation
    // ctx.beginPath();
    // Draw duration gradient
    const tick = Date.now();
    const timePassed = tick - startTime;
    const progress = timePassed / duration;
    const fullWidth = canvas.width / window.devicePixelRatio;
    const start = Math.round(progress * fullWidth);
    const center = canvas.height / window.devicePixelRatio / 2;
    const width = duration / 100;
    const end = start + width; // canvas.width
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
    // oscillator.stop();
    cancelAnimationFrame(cancelFrame);
  };
}

// Example usage:

export default modifier(function frequencyVisualizer(element, [option]) {
  const { duration, frequency } = option.signal;
  const cancel = draw(
    element,
    frequency * 1000,
    duration * 100,
    60,
    element.parentNode.clientWidth - 40,
    element.parentNode.clientHeight,
    option.word.split(':').pop(),
  );

  return () => {
    cancel();
  };
});
