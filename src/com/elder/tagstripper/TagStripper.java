package com.elder.tagstripper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;


public class TagStripper
{
	
	HashSet<String> validFrames = new HashSet<String> ();

	public void addValidFrame(String frame)
	{
		validFrames.add(frame);
	}

	
	public void strip(File file)
	{
		MP3File f;
		try
		{
			f = (MP3File)AudioFileIO.read(file);
		} 
		catch (CannotReadException e)
		{
			return;
		}
		catch (IOException e)
		{
			return;
		}
		catch (TagException e)
		{
			return;
		}
		catch (ReadOnlyFileException e)
		{
			return;
		}
		catch (InvalidAudioFrameException e)
		{
			return;
		}
		
		
		AbstractID3v2Tag v2tag  = f.getID3v2Tag();
				
		Iterator<TagField> fields = v2tag.getFields();
		TagField field;
		HashSet<TagField> toDelete = new HashSet<TagField> ();
		
		while (fields.hasNext())
		{
			field = fields.next();
			
			if (!validFrames.contains(field.getId()))
			{
				toDelete.add(field);
			}
						
		}
		
		for (TagField tagField : toDelete)
		{
			v2tag.removeFrame(tagField.getId());
		}
				
		if(f.hasID3v1Tag())
		{
			ID3v1Tag v1tag = f.getID3v1Tag();
			try
			{
				f.delete(v1tag);
			} 
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		v2tag.deleteArtworkField();
		
		try
		{
			f.commit();
		} 
		catch (CannotWriteException e)
		{
			
		}
				
	}
	
	public void crawl(File folder)
	{
		System.out.println(folder);
		for (File file : folder.listFiles())
		{
			if (file.getName().endsWith(".mp3"))
			{
				strip(file);
			}
			else if (file.isDirectory())
			{
				crawl(file);
			}
		}
	}
	
	public static void main(String args[])
	{
		TagStripper tagStripper = new TagStripper();
		tagStripper.addValidFrame("TALB");
		tagStripper.addValidFrame("TIT2");
		tagStripper.addValidFrame("TYER");
		tagStripper.addValidFrame("TPE2");
		tagStripper.addValidFrame("TPE1");
		tagStripper.crawl(new File(args[0]));
		
	}
}
