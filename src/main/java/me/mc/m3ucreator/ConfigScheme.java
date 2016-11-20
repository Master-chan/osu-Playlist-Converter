package me.mc.m3ucreator;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class ConfigScheme
{
	
	@Getter private String titleFormat;
	@Getter private File sourceDir;
	@Getter private int minSongLength;
	//@Getter private boolean ignoreLength;

}
