package eu.europeana.api2.v2.model.enums;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import eu.europeana.corelib.definitions.model.ThumbSize;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.utils.ImageUtils;

/**
 * @deprecated 2018-01-09 replaced by ThumbnailController.getDefaultThumbnailForNotFoundResourceByType
 */
@Deprecated
public enum DefaultImage {

	TT(ThumbSize.TINY, DocType.TEXT, "/images/item-text-tiny.gif"),
	TI(ThumbSize.TINY, DocType.IMAGE, "/images/item-image-tiny.gif"),
	TS(ThumbSize.TINY, DocType.SOUND, "/images/item-sound-tiny.gif"),
	TV(ThumbSize.TINY, DocType.VIDEO, "/images/item-video-tiny.gif"),
	T3(ThumbSize.TINY, DocType._3D, "/images/item-3d-tiny.gif"),
	MT(ThumbSize.MEDIUM, DocType.TEXT, "/images/item-text.gif"),
	MI(ThumbSize.MEDIUM, DocType.IMAGE, "/images/item-image.gif"),
	MS(ThumbSize.MEDIUM, DocType.SOUND, "/images/item-sound.gif"),
	MV(ThumbSize.MEDIUM, DocType.VIDEO, "/images/item-video.gif"),
	M3(ThumbSize.MEDIUM, DocType._3D, "/images/item-3d.gif"),
	LT(ThumbSize.LARGE, DocType.TEXT, "/images/item-text-large.gif"),
	LI(ThumbSize.LARGE, DocType.IMAGE, "/images/item-image-large.gif"),
	LS(ThumbSize.LARGE, DocType.SOUND, "/images/item-sound-large.gif"),
	LV(ThumbSize.LARGE, DocType.VIDEO, "/images/item-video-large.gif"),
	L3(ThumbSize.LARGE, DocType._3D, "/images/item-3d-large.gif"), 
	UNKNOWN(null, null, "/images/unknown.png");

	private ThumbSize size;
	private DocType type;
	private String image;
	private byte[] cache = null;

	DefaultImage(ThumbSize size, DocType type, String image) {
		this.size = size;
		this.type = type;
		this.image = image;
		loadImage();
	}

	private void loadImage() {
		try {
			BufferedImage buf = ImageIO.read(getClass().getResourceAsStream(
					image));
			cache = ImageUtils.toByteArray(buf);
		} catch (IOException e) {
			// ignore, unknown image will be provided by default behavior.
		}
	}

	public static byte[] getImage(ThumbSize size, DocType type) {
		for (DefaultImage image : DefaultImage.values()) {
			if ((image.size == size) && (image.type == type)) {
				return image.cache;
			}
		}
		return null;
	}

	public byte[] getCache() {
		return cache;
	}
}
