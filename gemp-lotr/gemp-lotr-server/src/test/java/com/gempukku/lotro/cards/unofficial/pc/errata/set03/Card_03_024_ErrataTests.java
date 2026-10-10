package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_024_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "53_24");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, vitality 3; archery ability only
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, vitality 2; text only at your site 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "53_24");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, vitality 3; archery ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Phial of Galadriel
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype:
		 * Game Text: To play, exert an Elf.  Bearer must be the Ring-bearer.
		 * Bearer's resistance is +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("phial");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayingExertsAnElfOfEitherKindAndGoesOnTheRingBearerForResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var legolas = scn.GetFreepsCard("legolas");
		var silinde = scn.GetFreepsCard("silinde");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(phial);
		scn.MoveCompanionsToTable(legolas, gimli, sam);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.FreepsPlayAvailable(phial));

		scn.FreepsPlayCard(phial);
		// An Elf companion and an Elf ally may pay; a Dwarf who could exert, a Hobbit Ring-bearer and Sam may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(silinde);

		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		// The only Ring-bearer is the only bearer: on Frodo, not on Sam.
		assertAttachedTo(phial, frodo);
		assertNotAttachedTo(phial, sam);
		assertEquals(0, scn.GetTwilight());
		assertEquals(12, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyElfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var legolas = scn.GetFreepsCard("legolas");
		var silinde = scn.GetFreepsCard("silinde");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(phial);
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();

		// Legolas, vitality 3 with 2 wounds, cannot exert; Gimli could, but is no Elf.
		assertTrue(scn.IsExhausted(legolas));
		assertFalse(scn.FreepsPlayAvailable(phial));

		scn.MoveAlliesToTable(silinde);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(phial));

		scn.FreepsPlayCard(phial);
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertAttachedTo(phial, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GoesOnARingBearerWhoIsNotFrodoAndNotOnTheCompanionBesideHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var phial = scn.GetFreepsCard("phial");
		var legolas = scn.GetFreepsCard("legolas");
		var galadriel = scn.GetRingBearer();
		scn.MoveCardsToHand(phial);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();

		// Galadriel bears The One Ring, so her own text already gives her +1 for each [elven] companion:
		// 3 + 2 (herself and Legolas). The Phial is no companion and does not add to that count.
		assertEquals(5, scn.GetResistance(galadriel));
		assertEquals(6, scn.GetResistance(legolas));

		scn.FreepsPlayCard(phial);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, galadriel));
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertAttachedTo(phial, galadriel);
		assertNotAttachedTo(phial, legolas);
		assertEquals(7, scn.GetResistance(galadriel));
		assertEquals(6, scn.GetResistance(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
