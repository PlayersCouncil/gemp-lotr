package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_057_Tests
{

	// No minion here is an archer, and Rúmil is away from his home site 6, so the archery phase places no wounds.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rumil", "1_57");
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: another Elf ally; a fellowship ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("hunter", "1_256");     // Morgul Hunter: [sauron] Orc, vitality 4; strength +1 for each companion
					put("savage", "1_151");     // Uruk Savage: an Uruk-hai, which is no Orc; damage +1 only
					// Half-troll of Far Harad: an [orc] minion who is no Orc, vitality 2; its trigger is at the
					// start of the assignment phase
					put("halftroll", "14_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RumilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Rúmil, Elven Protector
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Archer</b>.<br><b>Archery:</b> Exert Rúmil to wound an Orc.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rumil");

		assertEquals("Rúmil", card.getBlueprint().getTitle());
		assertEquals("Elven Protector", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void ArcheryActionExertsRumilToWoundAnOrcOfEitherCultureAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var celeborn = scn.GetFreepsCard("celeborn");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		var savage = scn.GetShadowCard("savage");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveAlliesToTable(rumil, celeborn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, hunter, savage, halftroll);

		// The maneuver phase, the one before: not offered there.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(hunter));
		assertTrue(scn.FreepsActionAvailable(rumil));

		scn.FreepsUseCardAction(rumil);
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, hunter));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, halftroll));
		scn.FreepsChooseCard(hunter);

		assertEquals(1, scn.GetWoundsOn(hunter));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ExhaustedRumilCannotUseTheAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var troop = scn.GetShadowCard("troop");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveAlliesToTable(rumil);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, hunter);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(rumil, 2);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));

		// The control: one wound taken off, and he can exert again.
		scn.RemoveWoundsFromChar(rumil, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(rumil));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
