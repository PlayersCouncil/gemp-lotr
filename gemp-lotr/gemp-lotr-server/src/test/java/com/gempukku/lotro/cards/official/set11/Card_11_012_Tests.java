package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "11_12");
					// Farin, Dwarven Emissary: a [dwarven] Dwarf companion, strength 5; his bonus is against Orcs only
					put("farin", "1_11");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6; his ability is not used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, strength 4, home 3F; his ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8; his ability is not used
				}},
				new HashMap<>()
				{{
					// The Fellowship-block path, with Pass of Caradhras (a mountain) as site 4 and Mount Doom (a
					// mountain that is also a battleground and underground) in place of site 9, which no test
					// reaches: Mount Doom is put on the path as the next site where a test wants it.
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_348");
					remove("site9");
					put("doom", "15_193");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountainHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mountain Homestead
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Dwarf companions.<br>While the fellowship is at a mountain site, each Dwarf is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("homestead");

		assertEquals("Mountain Homestead", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondDwarfCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(homestead, gimli);

		scn.StartGame();

		// One Dwarf companion; the Dwarf ally is no companion, and Aragorn and Frodo are no Dwarves.
		assertInHand(homestead, gimli);
		assertFalse(scn.FreepsPlayAvailable(homestead));

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);

		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachDwarfIsStrengthPlus2OnlyWhileTheFellowshipIsAtAMountainSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var farin = scn.GetFreepsCard("farin");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(farin, gimli, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);

		// Site 3, Rivendell Valley: no mountain.
		scn.SkipToSite(3);
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));    // 3 + 1 for The Ruling Ring

		// Site 4, Pass of Caradhras: a mountain. The two Dwarf companions and the Dwarf ally (at his home site
		// 3, not at the mountain: the text asks where the fellowship is) are +2; the others are not.
		scn.SkipToSite(4);
		assertEquals(7, scn.GetStrength(farin));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));

		// Site 5, The Bridge of Khazad-dum: underground and no mountain.
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSite().getBlueprint().getSiteNumber());
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachDwarfIsStrengthPlus2AtAMountainThatIsAlsoABattlegroundAndUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var doom = scn.GetFreepsSite("doom");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		scn.SkipToSite(3);
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(4, scn.GetStrength(thrarin));

		scn.MoveCardToAdventurePath(doom);
		scn.FreepsPass();
		assertEquals(doom, scn.GetCurrentSite());
		assertEquals(7, scn.GetStrength(farin));
		assertEquals(6, scn.GetStrength(thrarin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
