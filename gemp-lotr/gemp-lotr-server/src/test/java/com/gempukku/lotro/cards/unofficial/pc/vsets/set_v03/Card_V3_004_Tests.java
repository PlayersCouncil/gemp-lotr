package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hospitality", "103_4");
					put("pippin", "1_306"); // Unbound Hobbit
					put("sam", "1_311"); // Ring-bound Hobbit
					put("frodospipe", "3_107"); // Shire pipe
					put("gandalfspipe", "1_74"); // Gandalf pipe
					put("aragornspipe", "1_91"); // Gondor pipe

					// Companions of different cultures for culture count test
					put("gandalf", "1_364"); // Gandalf culture
					put("legolas", "1_50"); // Elven culture
					put("gimli", "1_13"); // Dwarven culture
					put("aragorn", "1_89"); // Gondor culture

					put("stalker", "5_72"); // Desert Stalker
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansHospitalityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Saruman's Hospitality
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Strength: 1
		 * Game Text: Pipeweed.
		* 	Bearer must be an unbound companion bearing a pipe. 
		* 	Each time the fellowship moves, spot 3 pipes of different cultures to make the number of Free Peoples cultures that can be counted -1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hospitality");

		assertEquals("Saruman's Hospitality", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}


	@Test
	public void HospitalityRequiresUnboundCompanionWithPipeAndGivesStrength() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var frodospipe = scn.GetFreepsCard("frodospipe");

		scn.MoveCardsToHand(hospitality);
		scn.MoveCompanionsToTable(pippin, sam);
		scn.AttachCardsTo(pippin, frodospipe);

		scn.StartGame();

		// Pippin is unbound with pipe, base strength should be whatever it is
		int pippinBaseStrength = scn.GetStrength(pippin);

		// Should be able to play on Pippin (unbound with pipe)
		assertTrue(scn.FreepsPlayAvailable(hospitality));
		scn.FreepsPlayCard(hospitality);
		assertAttachedTo(hospitality, pippin);

		// Pippin should have +1 strength from hospitality
		assertEquals(pippinBaseStrength + 1, scn.GetStrength(pippin));
	}

	@Test
	public void HospitalityCannotAttachToRingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var hospitality = scn.GetFreepsCard("hospitality");
		var sam = scn.GetFreepsCard("sam");
		var frodospipe = scn.GetFreepsCard("frodospipe");

		scn.MoveCardsToHand(hospitality);
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, frodospipe);

		scn.StartGame();

		// Sam is ring-bound, so Hospitality should not be playable
		// (Frodo is also ring-bound and is always present)
		assertFalse(scn.FreepsPlayAvailable(hospitality));
	}

	@Test
	public void HospitalityReducesCultureCountWith3DifferentPipes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer(); // Shire culture
		var gandalf = scn.GetFreepsCard("gandalf"); // Gandalf culture
		var legolas = scn.GetFreepsCard("legolas"); // Elven culture
		var gimli = scn.GetFreepsCard("gimli"); // Dwarven culture
		var hospitality = scn.GetFreepsCard("hospitality");
		var pippin = scn.GetFreepsCard("pippin");
		var frodospipe = scn.GetFreepsCard("frodospipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");

		scn.MoveCompanionsToTable(gandalf, legolas, gimli, pippin);
		scn.AttachCardsTo(pippin, frodospipe, hospitality);
		scn.AttachCardsTo(gandalf, gandalfspipe);
		// Only 2 pipes initially

		var stalker = scn.GetShadowCard("stalker");
		scn.MoveMinionsToTable(stalker);

		scn.StartGame();

		// Move from site 1 to site 2 - only 2 pipes, so modifier doesn't trigger
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPass();
		// Desert Stalker should be able to use ability (spots 4 cultures: Shire, Gandalf, Elven, Dwarven)
		assertTrue(scn.ShadowActionAvailable(stalker));

		// Now attach 3rd pipe directly
		scn.AttachCardsTo(legolas, aragornspipe);

		// Move during Regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		scn.BothPass();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());

		// Now Hospitality should trigger (3 pipes of different cultures)
		// FP culture count is reduced by 1, so Stalker sees only 3 cultures instead of 4
		scn.SkipToPhase(Phase.MANEUVER);

		// Desert Stalker should NOT be able to use ability (needs 4 cultures, only sees 3)
		assertFalse(scn.ShadowActionAvailable(stalker));
	}
}
