package me.mc.m3ucreator;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JProgressBar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.mpatric.mp3agic.Mp3File;

@RequiredArgsConstructor
public class M3UCreator implements Runnable
{

	private static Pattern OSUCONFIG = Pattern.compile("(.*)(\\.osu)");

	private static boolean matches(String s, Pattern p)
	{
		Matcher m = p.matcher(s);
		return m.matches();
	}

	private final ConfigScheme config;
	private List<OsuMP3Scheme> mp3List;
	final private JProgressBar progressBar;
	
	// Results
	@Getter private int songsProcessed = 0;
	@Getter private int songsDropped = 0;

	@Override
	public void run()
	{
		mp3List = new ArrayList<>(512);
		scanFolder(config.getSourceDir());
		songsProcessed = mp3List.size();
		String writeString = generateString();
		File output = new File(config.getSourceDir().getParent(), "myOsuPlaylist.m3u");
		if(output.exists())
		{
			output.delete();
		}
		try
		{
			output.createNewFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		FileWriter aWriter;
		try
		{
			aWriter = new FileWriter(output, false);
			aWriter.write(writeString);
			aWriter.flush();
			aWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private String generateString()
	{
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("#EXTM3U\r\n");
		outputBuilder.append("#PLAYLIST:My osu! playlist.\r\n");
		
		String extinfTemplate = "#EXTINF:%length%," + config.getTitleFormat() + "\r\n";
		
		for(OsuMP3Scheme scheme : mp3List)
		{	
			String extinf = extinfTemplate
					.replaceAll(Pattern.quote("%length%"), String.valueOf(scheme.getLength()))
					.replaceAll(Pattern.quote("%artist%"), Matcher.quoteReplacement(scheme.getArtist()))
					.replaceAll(Pattern.quote("%title%"), Matcher.quoteReplacement(scheme.getTitle()))
					.replaceAll(Pattern.quote("%artistUnicode%"), scheme.getArtistUnicode() != null && scheme.getArtistUnicode().length() > 1 ? 
							Matcher.quoteReplacement(scheme.getArtistUnicode()) : Matcher.quoteReplacement(scheme.getArtist()))
					.replaceAll(Pattern.quote("%titleUnicode%"), scheme.getTitleUnicode() != null && scheme.getTitleUnicode().length() > 1 ? 
							Matcher.quoteReplacement(scheme.getTitleUnicode()) : Matcher.quoteReplacement(scheme.getTitle()))
					.replaceAll(Pattern.quote("%source%"), scheme.getSource() != null && scheme.getSource().length() > 1 ? 
							Matcher.quoteReplacement(scheme.getSource()) : "");
			outputBuilder.append(extinf);
			outputBuilder.append(scheme.getFile().getAbsolutePath() + "\r\n");
		}
		return outputBuilder.toString();
	}

	private void scanFolder(File folder)
	{
		final File[] listFiles = folder.listFiles();
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				progressBar.setMaximum(listFiles.length);
				progressBar.setValue(0);
			}
		});
		for(File localFile : listFiles)
		{
			if(localFile.isDirectory())
			{
				for(File osuFile : localFile.listFiles())
				{
					if(matches(osuFile.getName(), OSUCONFIG))
					{
						OsuMP3Scheme scheme = scanOsuFile(osuFile);
						if(scheme != null)
						{
							mp3List.add(scheme);
						}
						EventQueue.invokeLater(new Runnable()
						{
							public void run()
							{
								try
								{
									progressBar.setValue(progressBar.getValue() + 1);
								}
								catch(Exception e)
								{
									e.printStackTrace();
									System.exit(1);
								}
							}
						});
						break;
					}
				}
			}
		}
	}

	private OsuMP3Scheme scanOsuFile(File osuFile)
	{
		OsuMP3Scheme scheme = new OsuMP3Scheme();
		try(InputStream inputStream = new FileInputStream(osuFile))
		{
			try(BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));)
			{
				String line;
				while((line = inputReader.readLine()) != null)
				{
					if(line.startsWith("AudioFilename: "))
					{
						scheme.setFile(new File(osuFile.getParent(), line.replaceFirst("AudioFilename: ", "")));
					}
					else if(line.startsWith("Title:"))
					{
						scheme.setTitle(line.replaceFirst("Title:", ""));
					}
					else if(line.startsWith("Artist:"))
					{
						scheme.setArtist(line.replaceFirst("Artist:", ""));
					}
					else if(line.startsWith("TitleUnicode:"))
					{
						scheme.setTitleUnicode(line.replaceFirst("TitleUnicode:", ""));
					}
					else if(line.startsWith("ArtistUnicode:"))
					{
						scheme.setArtistUnicode(line.replaceFirst("ArtistUnicode:", ""));
					}
					else if(line.startsWith("Source:"))
					{
						scheme.setSource(line.replaceFirst("Source:", ""));
					}
					
					if(line.startsWith("[TimingPoints]") || line.startsWith("[Events]"))
					{
						break;
					}
				}
			}

			Mp3File mp3file = new Mp3File(scheme.getFile());
			scheme.setLength(mp3file.getLengthInSeconds());
			
			if(scheme.getLength() < config.getMinSongLength())
			{
				songsDropped++;
				return null;
			}
		}
		catch(Exception e)
		{
		}

		return scheme;
	}

}
