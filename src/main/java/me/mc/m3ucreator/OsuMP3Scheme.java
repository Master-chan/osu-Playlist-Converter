package me.mc.m3ucreator;

import java.io.File;

import lombok.Getter;
import lombok.Setter;


public class OsuMP3Scheme
{
	
	@Getter @Setter private File file;
	@Getter @Setter private long length;
	
	@Getter @Setter private String artist;
	@Getter @Setter private String title;
	@Getter @Setter private String artistUnicode;
	@Getter @Setter private String titleUnicode;
	@Getter @Setter private String source;
	
}
