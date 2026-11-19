package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_207_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("terror", "7_207");
					// Úlairë Lemenya: a [wraith] Nazgûl, vitality 2; her text is about [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], vitality 3
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, vitality 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");

					// Bounder: a [shire] Hobbit ally, vitality 2, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Orophin, Lórien Bowman: an [elven] Elf ally, vitality 3, home 6F; an archery ability used
					// only on purpose
					put("orophin", "1_56");
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirPowerIsinTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Their Power Is in Terror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert a Nazgûl to wound each ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("terror");

		assertEquals("Their Power Is in Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsANazgulOfEitherCultureToWoundEachAllyAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var orophin = scn.GetFreepsCard("orophin");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(bounder, orophin);
		scn.MoveCompanionsToTable(sam);

		var terror = scn.GetShadowCard("terror");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(terror);
		assertInHand(terror);

		scn.StartGame();
		// Bounder, vitality 2, is exhausted: the wound kills him.
		scn.AddWoundsToChar(bounder, 1);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		assertEquals(1, scn.GetWoundsOn(bounder));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.ShadowPlayAvailable(terror));
		scn.ShadowPlayCard(terror);

		// A Nazgûl of [wraith] and one of [raider]; a [wraith] Orc is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.DEAD, bounder);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(terror);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(orophin);

		var terror = scn.GetShadowCard("terror");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(terror);
		assertInHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		// Lemenya, vitality 2, is exhausted; the Hound, unwounded, could exert but is no Nazgûl.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInHand(terror);
		assertFalse(scn.ShadowPlayAvailable(terror));
		assertEquals(0, scn.GetWoundsOn(orophin));
	}

	@Test
	public void WithNoAllyInPlayItIsPlayedAndTheNazgulExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		var terror = scn.GetShadowCard("terror");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(terror);
		assertInHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertTrue(scn.ShadowPlayAvailable(terror));
		scn.ShadowPlayCard(terror);

		// The one Nazgûl exerts unasked; nobody is wounded.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertInDiscard(terror);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
