import { modifier } from 'ember-modifier';

const canvas = document.createElement('canvas');
const ctx: CanvasRenderingContext2D  = canvas.getContext('2d') as CanvasRenderingContext2D;

function width(str: string, font: string) {
  if (font) { ctx.font = font; }
  return ctx.measureText(str).width;
};

function fitTextSize(string: string, maxWidth: number, font: string, rawFontSize: string) {
  let w = width(string, font);
  if (w < maxWidth) {
    return 0;
  }
  let fontSize = parseFloat(rawFontSize.match(/\d+/)[0]);
  return Math.floor(fontSize * maxWidth / w);
}

export default modifier(function autofitText(element: HTMLDivElement) {
  const style = getComputedStyle(element);
  const fontFamily = style.getPropertyValue('font-family');
  const fontSize = style.getPropertyValue('font-size');
  const fontWeight = style.getPropertyValue('font-weight');
  const fontStyle =  style.getPropertyValue('font-style');
  const fontVariant = style.getPropertyValue('font-variant');
  console.log(style.getPropertyValue('padding-left'));
  console.log(style.getPropertyValue('padding-right'));
  console.log(style.getPropertyValue('text-transform'));
  let text = (element.textContent || '').toString().trim();
  const transform = style.getPropertyValue('text-transform');
  if (transform === 'uppercase') {
    text = text.toUpperCase();
  }

  const font = `${fontStyle} ${fontVariant} ${fontWeight} ${fontSize} ${fontFamily}`;
  const newFontSize = fitTextSize(text, element.clientWidth - 20, font, fontSize);
  if (newFontSize) {
    element.style.fontSize = `${newFontSize - 1}px`;
  }
});
