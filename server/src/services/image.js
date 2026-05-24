const sharp = require('sharp');

async function compressImage(buffer) {
  const metadata = await sharp(buffer).metadata();

  const resized = await sharp(buffer)
    .resize({ width: Math.min(metadata.width, 1600), withoutEnlargement: true })
    .jpeg({ quality: 80 })
    .toBuffer();

  const resultMeta = await sharp(resized).metadata();

  return { buffer: resized, width: resultMeta.width, height: resultMeta.height };
}

async function generateThumbnail(buffer) {
  return sharp(buffer)
    .resize(400, 400, { fit: 'inside', withoutEnlargement: true })
    .jpeg({ quality: 70 })
    .toBuffer();
}

module.exports = { compressImage, generateThumbnail };
