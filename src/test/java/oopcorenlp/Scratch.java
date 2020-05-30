/*******************************************************************************
 * Copyright (C) 2020 Ram Sadasiv
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package oopcorenlp;

import java.util.regex.Pattern;

public class Scratch {

	public Scratch() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String input = "Although Ada had passed thirty, she was still going on holidays with her parents. This time they went for a two-week trip to one of the coastal resorts in Goa.\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"Ada’s parents and her brother found the place much to their taste and in their typical way enjoyed the touristy pleasures: swimming, diving (although this was only for the boys), eating out and feeding cows and other stray animals. To this list Ada’s mother added shopping. Ada was sceptical about the last activity, but Ada’s mother pre-empted her criticism saying, ‘I don’t strive to be authentic and I don’t mind appropriating their culture, not least because they are happy for me to do it. Let me be post-colonial and I’m okay with you being woke.’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"As there wasn’t much else to do in the village apart from swimming and doing things on her smartphone, after a week Ada decided to accompany her mother shopping.\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘For sure this is the case, but I don’t mind if they don’t appreciate my uniqueness either. Buying a scarf or a pair of earrings is not necessarily like making love, even if they try to make you feel as if it is. They follow their own rituals and I follow mine.’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"*\n" + 
				"\n" + 
				"‘All the shops and all the vendors are the same to me,’ she said.\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘My name is Kaushal, which means ‘smart’. What is your name?’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘Ada.’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"What does it mean?’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘I don’t know, but probably not ‘smart’.’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"The guy laughed in a somewhat forced way.\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘I am from Kashmir and everything what I sell here is from Kashmir. Do you know Kashmir?’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘Not really. The only Kashmir I know is the one from Led Zeppelin’s song.’\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘Sorry, I don’t know it,’ said Kaushal.\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"‘Don’t worry, it’s an old song. Even young English people don’t know it.’";
		
		//input = input.replaceAll("^\\u2018", "\"");
		//input = input.replaceAll("\\u2019$", "\"");
		input = Pattern.compile("^\\u2018", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("\\s\\u2018(\\S)", Pattern.MULTILINE).matcher(input).replaceAll(" \"$1");
		input = Pattern.compile("\\u2019$", Pattern.MULTILINE).matcher(input).replaceAll("\"");
		input = Pattern.compile("(\\S)\\u2019\\s", Pattern.MULTILINE).matcher(input).replaceAll("$1\" ");
		input = Pattern.compile("\\u2019(\\p{Punct})", Pattern.MULTILINE).matcher(input).replaceAll("\"$1");
		System.out.println(input);

	}

}
