package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("honoring", "13_6");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1; a skirmish ability, never used
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2;
												// "Maneuver: Exert Thrarin to allow him to participate in archery fire and skirmishes until the regroup phase."
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability only

					// For the discard pile.
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, hand weapon, cost 0, bearer must be a Dwarf
					put("bracers", "12_5");     // Dwarven Bracers: [dwarven] possession, bracers, cost 1, bearer must be a Dwarf
					put("ring", "9_9");         // Ring of Retribution: [dwarven] artifact on a Dwarf, cost 0
					put("wish", "8_8");         // Wish For Our Kinfolk: [dwarven] support-area condition, cost 1
					put("toby", "1_305");       // Old Toby: [shire] support-area possession, cost 1
					put("handaxe", "2_10");     // Hand Axe: [dwarven] possession; the Shadow player's copy lies in his own discard pile

					// Cards that hold culture tokens; each gets them only as it is played.
					put("palaces", "6_10");     // Suspended Palaces: [dwarven] condition, a fellowship ability only
					put("chamber", "15_3");     // Chamber of Records: [dwarven] condition, a fellowship ability only
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession with [elven] tokens, a fellowship ability only

					// Fierce minions with nothing else that reaches these boards.
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("pursuer2", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HonoringHisKinfolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Honoring His Kinfolk
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf wins a fierce skirmish, play a [dwarven] possession from your discard pile and you may reinforce a [dwarven] token.
		*/

		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");

		assertEquals("Honoring His Kinfolk", honoring.getBlueprint().getTitle());
		assertNull(honoring.getBlueprint().getSubtitle());
		assertFalse(honoring.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, honoring.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, honoring.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, honoring.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(honoring, Timeword.RESPONSE));
		assertEquals(1, honoring.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarfWhoWinsAFierceSkirmishPlaysADwarvenPossessionFromTheDiscardPileAndMayReinforceADwarvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var ring = scn.GetFreepsCard("ring");
		var wish = scn.GetFreepsCard("wish");
		var toby = scn.GetFreepsCard("toby");
		var palaces = scn.GetFreepsCard("palaces");
		var chamber = scn.GetFreepsCard("chamber");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces, chamber, strands);
		scn.AddTokensToCard(palaces, 2);
		scn.AddTokensToCard(chamber, 1);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(honoring);
		scn.MoveCardsToDiscard(axe, bracers, ring, wish, toby);

		var pursuer = scn.GetShadowCard("pursuer");
		var handaxe = scn.GetShadowCard("handaxe");
		scn.MoveCardsToDiscard(handaxe);

		scn.StartGame();
		assertInDiscard(axe, bracers, ring, wish, toby, handaxe);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(1, scn.GetCultureTokensOn(chamber));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		scn.MoveMinionsToTable(pursuer);

		// Nobody is assigned in the first round; the Pursuer's fierce skirmish follows.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		// Gimli 6 beats the Pursuer 5 in its fierce skirmish.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsPlayAvailable(honoring));
		scn.FreepsPlayCard(honoring);

		// [dwarven] possessions of two classes; not a [dwarven] artifact, a [dwarven] condition, a [shire] possession,
		// nor the Shadow player's [dwarven] possession in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, bracers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, wish, toby, handaxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bracers);
		assertAttachedTo(bracers, gimli);
		assertInDiscard(axe, ring, wish, toby);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		// Cards with a [dwarven] token; not the card with [elven] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(palaces, chamber));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(strands));
		scn.FreepsChooseCard(chamber);

		assertEquals(2, scn.GetCultureTokensOn(chamber));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertInDiscard(honoring);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWhenADwarfWinsTheFirstRoundAndTheReinforceMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(palaces);
		scn.AddTokensToCard(palaces, 2);
		scn.MoveCardsToHand(honoring);
		scn.MoveCardsToDiscard(axe);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		assertInDiscard(axe);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// Gimli 6 beats the fierce Pursuer 5 in the first round: that skirmish is not a fierce one.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		assertFalse(scn.FreepsPlayAvailable(honoring));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		// He beats it again in its fierce skirmish, and kills it.
		assertInDiscard(pursuer);
		assertTrue(scn.FreepsPlayAvailable(honoring));
		scn.FreepsPlayCard(honoring);
		assertAttachedTo(axe, gimli);

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertInDiscard(honoring);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlayableWhenADwarfAllyWinsAFierceSkirmishAndNotWhenAManDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var axe = scn.GetFreepsCard("axe");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(palaces);
		scn.AddTokensToCard(palaces, 2);
		scn.MoveCardsToHand(honoring);
		scn.MoveCardsToDiscard(axe);

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");

		scn.StartGame();
		assertInDiscard(axe);
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(pursuer, pursuer2);
		scn.SkipToPhase(Phase.MANEUVER);

		// Thrarin takes part in skirmishes. A strength of 7, by hand, lets him beat a Pursuer.
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 3));

		// Nobody is assigned in the first round.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, pursuer}, new PhysicalCardImpl[]{thrarin, pursuer2});

		// Aragorn 8, a Man, beats a Pursuer in its fierce skirmish: not playable.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsPlayAvailable(honoring));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thrarin, a Dwarf ally, beats the other: playable, and the Axe goes on him.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassFierceSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer2));
		assertTrue(scn.FreepsPlayAvailable(honoring));
		scn.FreepsPlayCard(honoring);
		assertAttachedTo(axe, thrarin);

		scn.FreepsChooseNo();
		assertInDiscard(honoring);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithNoDwarvenPossessionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");
		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		var wish = scn.GetFreepsCard("wish");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(honoring);
		scn.MoveCardsToDiscard(ring, wish, toby);

		var pursuer = scn.GetShadowCard("pursuer");
		var handaxe = scn.GetShadowCard("handaxe");
		scn.MoveCardsToDiscard(handaxe);

		scn.StartGame();
		assertInDiscard(ring, wish, toby, handaxe);
		assertInHand(honoring);
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		// Gimli wins the fierce skirmish, but there is no [dwarven] possession in the pile to play.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsPlayAvailable(honoring));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(honoring);
	}

	@Test
	public void WithNoDwarvenTokenInPlayTheReinforceIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var honoring = scn.GetFreepsCard("honoring");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var strands = scn.GetFreepsCard("strands");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(honoring);
		scn.MoveCardsToDiscard(axe);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		assertInDiscard(axe);
		assertEquals(2, scn.GetCultureTokensOn(strands));
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		scn.FreepsPlayCard(honoring);
		assertAttachedTo(axe, gimli);

		// Only [elven] tokens are in play: there is nothing to reinforce, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("reinforce"));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertInDiscard(honoring);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
