package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("picket1", "13_118");
					put("picket2", "13_118");

					put("denizen", "11_115");    // Denizen of Khazad-dûm: an [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					// Half-troll of Far Harad: an [orc] minion who is no Orc, vitality 2; its start-of-assignment trigger
					// needs more companions than minions, never so here
					put("halftroll", "14_12");
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("defend", "4_115");      // Defend It and Hope: [gondor] Maneuver event, cost 0: exert a [gondor] Man to wound a minion
					                             // once (or twice if that minion is an Uruk-hai)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PicketDenizenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Picket Denizen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Response:</b> If another [orc] minion is about to take a wound, exert this minion to prevent that.
		*/

		var scn = GetScenario();

		var picket1 = scn.GetFreepsCard("picket1");

		assertEquals("Picket Denizen", picket1.getBlueprint().getTitle());
		assertNull(picket1.getBlueprint().getSubtitle());
		assertFalse(picket1.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, picket1.getBlueprint().getSide());
		assertEquals(Culture.ORC, picket1.getBlueprint().getCulture());
		assertEquals(CardType.MINION, picket1.getBlueprint().getCardType());
		assertEquals(Race.ORC, picket1.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(picket1, Keyword.LURKER));
		assertEquals(3, picket1.getBlueprint().getTwilightCost());
		assertEquals(9, picket1.getBlueprint().getStrength());
		assertEquals(2, picket1.getBlueprint().getVitality());
		assertEquals(4, picket1.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyOffersToPreventTheWoundAndOnlyTheCopyUsedExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var picket1 = scn.GetShadowCard("picket1");
		var picket2 = scn.GetShadowCard("picket2");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(picket1, picket2, denizen, halftroll);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(picket2));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));

		scn.FreepsPlayCard(defend);
		// Aragorn, the one [gondor] Man, exerts; the Denizen is no Uruk-hai and is to be wounded once.
		scn.FreepsChooseCard(denizen);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(picket1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(picket2));
		scn.ShadowChooseOptionalTrigger(picket1);

		assertEquals(1, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(picket2));
		assertEquals(0, scn.GetWoundsOn(denizen));
		// The Half-troll, an [orc] minion that was not about to be wounded, is not touched.
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AmongTheLosersOfOneSkirmishOnlyOrcCultureMinionsCanBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var picket1 = scn.GetShadowCard("picket1");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(picket1, denizen, halftroll, troop);
		// Only so that Aragorn beats all four: 8 + 40 against 9 + 9 + 8 + 13.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 40));
		scn.SkipToAssignments();
		// One minion is the Free Peoples player's to assign to a companion; the Shadow player adds the others.
		scn.FreepsAssignToMinions(aragorn, denizen);
		scn.ShadowAssignToMinions(aragorn, halftroll, troop, picket1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The four losers are about to take a wound each, together.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(picket1));
		scn.ShadowChooseOptionalTrigger(picket1);
		assertEquals(1, scn.GetWoundsOn(picket1));
		// An [orc] Orc and an [orc] Half-troll may be saved; a [moria] Orc may not, nor the Picket himself, though
		// he too is about to take a wound.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, picket1));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		// Vitality 2: the exertion and his own wound from the lost skirmish kill him, and he is not offered again; the
		// other two take their wounds.
		assertInDiscard(picket1);
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWoundOnThePicketHimselfIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var picket1 = scn.GetShadowCard("picket1");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(picket1, denizen);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(denizen));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(picket1);

		// "Another" [orc] minion: he can exert, but his own wound is not answered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWoundOnAnOrcOfAnotherCultureIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var picket1 = scn.GetShadowCard("picket1");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(picket1, troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(troop);

		// The [moria] Orc takes its wound unanswered, though the Picket could exert.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(picket1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileThePicketIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var picket1 = scn.GetShadowCard("picket1");
		var denizen = scn.GetShadowCard("denizen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.MoveMinionsToTable(picket1, denizen);
		// Vitality 2 with 1 wound: he cannot exert.
		scn.AddWoundsToChar(picket1, 1);
		assertEquals(1, scn.GetWoundsOn(picket1));
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(denizen);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(picket1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
