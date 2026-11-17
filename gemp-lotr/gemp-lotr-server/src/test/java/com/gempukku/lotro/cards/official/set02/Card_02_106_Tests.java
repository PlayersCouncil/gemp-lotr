package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("imitation", "2_106");
					put("imitation2", "2_106");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3, vitality 4; a skirmish ability never used
					// Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion (cheated in);
					// a skirmish ability never used.
					put("trollslayer", "103_31");
					// Smeagol, Once Like Us: a [shire] companion with no race; his assignment ability is never reached.
					put("smeagol", "103_116");
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, vitality 2; a skirmish ability never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, vitality 2; a maneuver ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man who exerts for Defend It and Hope; a maneuver ability never used
					put("defend", "4_115");     // Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion (twice an Uruk-hai)

					put("savage", "1_151");     // Uruk Savage: strength 5, damage +1, and nothing else
					put("marksman1", "1_176");  // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("assassin", "2_93");    // Tower Assassin: vitality 3; "Maneuver: Exert Tower Assassin to wound an ally."
					// Ted Sandyman, Chief's Men's Ally: the Shadow player's Hobbit, a [men] minion of vitality 4; a maneuver
					// ability that needs a site he controls and a regroup ability, neither used.
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NiceImitationStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Nice Imitation
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Stealth</b>. Plays to your support area.
		 * 	<b>Response:</b> If a Hobbit is about to take a wound, discard this condition to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("imitation");

		assertEquals("Nice Imitation", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EitherCopyIsDiscardedToPreventOneWoundOfALostSkirmishAndTheOtherWoundIsNotPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");
		var imitation2 = scn.GetFreepsCard("imitation2");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(imitation, imitation2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		assertInZone(Zone.SUPPORT, imitation, imitation2);
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);
		// Merry (3) loses to the Savage (5, damage +1, and not twice his strength): two wounds, one at a time.
		scn.PassCurrentPhaseActions();

		// The first wound: each copy offers itself.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation2));
		scn.FreepsChooseOptionalTrigger(imitation);
		assertInDiscard(imitation);
		assertInZone(Zone.SUPPORT, imitation2);
		assertEquals(0, scn.GetWoundsOn(merry));

		// "That wound" only: the second is a wound of its own, offered to the copy still in play, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation2));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.SUPPORT, imitation2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsAnArcheryWoundToAHobbitOfAnotherCultureAndIsNotOfferedForAShireCompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(trollslayer, smeagol);
		scn.MoveCardsToSupportArea(imitation);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman1", "marksman2");

		assertInZone(Zone.SUPPORT, imitation);
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(smeagol));

		scn.SkipToArcheryWounds();
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The first arrow is for Smeagol, a [shire] companion who is no Hobbit: not offered.
		scn.FreepsChooseCard(smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(imitation));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.SUPPORT, imitation);

		// The second is for Pippin, a [gondor] Hobbit: offered, and the wound is prevented.
		scn.FreepsChooseCard(trollslayer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(imitation);
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PreventsAWoundToAHobbitWhoIsAnAllyAndIsNotOfferedForADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");
		var bounder = scn.GetFreepsCard("bounder");
		var thrarin = scn.GetFreepsCard("thrarin");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveAlliesToTable(bounder, thrarin);
		scn.MoveCardsToSupportArea(imitation);

		scn.StartGame();
		scn.MoveMinionsToTable(assassin);

		assertInZone(Zone.SUPPORT, imitation);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// A Dwarf ally first: not offered.
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(bounder, thrarin));
		scn.ShadowChooseCard(thrarin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(imitation));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInZone(Zone.SUPPORT, imitation);

		// Then Bounder, a Hobbit who is an ally: offered, and the wound is prevented.
		scn.FreepsPass();
		scn.ShadowUseCardAction(assassin);
		scn.ShadowChooseCard(bounder);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(imitation);
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(2, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void PreventsAWoundToTheShadowPlayersHobbitMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(imitation);
		scn.MoveCardsToHand(defend);

		var ted = scn.GetShadowCard("ted");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage);

		assertInZone(Zone.SUPPORT, imitation);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(ted));

		// Aragorn is the one [gondor] Man, and exerts with nobody asked.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ted, savage));
		scn.FreepsChooseCard(ted);

		// A race word names no side (Q-0058): the Shadow player's Hobbit is a Hobbit too.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(imitation);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void PreventsOnlyTheHobbitMinionsWoundWhenAnotherMinionLosesTheSameSkirmishWithHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imitation = scn.GetFreepsCard("imitation");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(imitation);

		var ted = scn.GetShadowCard("ted");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage);
		assertInZone(Zone.SUPPORT, imitation);
		// Aragorn, 8 + 4, beats the two together, 3 + 5, and is far from doubling them: neither is overwhelmed,
		// and the two lose together and are wounded together, in one window.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ted);
		scn.ShadowAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// Ted, a Hobbit, and the Savage, who is none, are about to take a wound together.  Ted is the only Hobbit
		// minion in print, so the wound to be prevented is chosen for the player.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imitation));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(imitation);
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
