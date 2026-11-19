package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("prowling", "11_136");

					// Denizen of Khazad-dum: an [orc] Orc, strength 9.  Strength +2 only at an underground site, never
					// reached.
					put("denizen", "11_115");
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8.  Its trigger at the start of
					// the assignment phase needs more companions than minions, never so here.
					put("halftroll", "14_12");
					// Goblin Patrol Troop: a [moria] Orc, strength 13, no text.
					put("troop", "1_177");
					// The Beckoning Shadow: an [orc] support-area condition, no minion (nothing below 5 burdens).
					put("beckoning", "12_83");

					// Aragorn, Ranger of the North: strength 8.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6.  A skirmish ability, never used.
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ProwlingOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Prowling Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Remove (2) to make an [orc] minion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("prowling");

		assertEquals("Prowling Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemoves2ToMakeAnOrcMinionStrengthPlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var prowling = scn.GetShadowCard("prowling");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToSupportArea(beckoning);
		assertInZone(Zone.SUPPORT, beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(prowling, denizen, halftroll, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, prowling}, new PhysicalCardImpl[]{gimli, denizen});
		// The Half-troll and the Troop are left unassigned.
		scn.ShadowDeclineAssignments();

		// The lurker's skirmish waits for Gimli's.
		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(prowling));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(8, scn.GetStrength(halftroll));

		scn.ShadowUseCardAction(prowling);
		assertEquals(3, scn.GetTwilight());
		// The minion in this skirmish, the lurker in the skirmish still waiting and a minion left unassigned (of
		// two races); not a [moria] Orc, nor an [orc] card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, prowling, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertEquals(10, scn.GetStrength(denizen));
		assertEquals(6, scn.GetStrength(prowling));
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Gimli, 6 to 10, lost and took a wound.
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(9, scn.GetStrength(denizen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithOnly1InThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var prowling = scn.GetShadowCard("prowling");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(prowling, denizen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, prowling}, new PhysicalCardImpl[]{gimli, denizen});

		scn.FreepsResolveSkirmish(gimli);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(prowling));
	}

	@Test
	public void NotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var prowling = scn.GetShadowCard("prowling");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveMinionsToTable(prowling, denizen);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The Shadow player's assignment actions, the pool able to pay.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(prowling));
	}
}
