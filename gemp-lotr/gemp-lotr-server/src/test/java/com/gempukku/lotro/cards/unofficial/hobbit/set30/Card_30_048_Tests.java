package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_048_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 (strength 3, vitality 4) as the
	// Ring-bearer, and no card in the Ring slot: this One Ring is a draw-deck artifact the test puts on Bilbo.
	// The skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer. A doubt
	// is the Hobbit game's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ring", "30_48");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4; +1 for each [dwarven] follower he bears
					// (none) and a fellowship ability
					put("thorin", "30_19");
					// Master Worrywort, Hobbit Farmer: a [shire] Hobbit ally who is no Bilbo; laid only where the
					// Ring is played
					put("worrywort", "33_53");

					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
					put("runner2", "31_30");
					// Wicked Spider: a fierce [spider] Spider, 7/2; a skirmish ability used only on purpose
					put("spider", "31_55");
					// Fimbul, Orkish Assassin: [gundabad] Orc, 7/2, damage +1; a play requirement and a Shadow
					// ability, neither reached (cheated in)
					put("fimbul", "30_33");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void TheOneRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: The One Ring
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Resistance: +1
		 * Game Text: Bearer must be Bilbo.
		 * 	While wearing The One Ring, Bilbo is strength +2, and each time he is about to take a wound in a
		 * 	skirmish, add a doubt instead.
		 * 	<b>Skirmish:</b> Add a doubt to wear The One Ring until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ring");

		assertEquals("The One Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnlyOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);

		scn.StartGame();
		scn.MoveCardsToHand(ring);
		scn.FreepsRefreshDecision();
		assertInHand(ring);

		assertTrue(scn.FreepsPlayAvailable(ring));
		scn.FreepsPlayCard(ring);

		// Neither the Dwarf companion nor the Hobbit ally may bear it: Bilbo is the one bearer, and nothing is asked.
		assertAttachedTo(ring, bilbo);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(worrywort).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionAddsADoubtToWearTheRingAndBilboIsStrengthPlus2UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, ring);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		scn.MoveMinionsToTable(runner, spider);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The assignment phase is no skirmish: the Ring's action is not among the Free Peoples actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ring));
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(bilbo, runner);
		// The fierce Spider is left out of the first round.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(bilbo);

		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(7, scn.GetStrength(thorin));
		assertTrue(scn.FreepsActionAvailable(ring));
		scn.FreepsUseCardAction(ring);

		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(bilbo));
		// Thorin bears no Ring.
		assertEquals(7, scn.GetStrength(thorin));

		// Bilbo, 5, ties the Runner, 5, and loses: a doubt for the wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetBurdens());

		// The fierce skirmish follows the one already resolved, and the Ring is still worn.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(5, scn.GetStrength(bilbo));
		// Bilbo, 5, loses to the Spider, 7: a doubt again.
		scn.PassFierceSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetBurdens());

		// The first decision of the regroup phase: the Ring is off and the bonus is gone.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertEquals(3, scn.GetStrength(bilbo));
		assertAttachedTo(ring, bilbo);
	}

	@Test
	public void NotWornALossIsTwoWoundsAndWornTheSameLossIsTwoDoubtsWhileAnotherCompanionsWoundStaysAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fimbul = scn.GetShadowCard("fimbul");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, ring);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		scn.MoveMinionsToTable(fimbul, runner);
		// Fierce by hand, so that Fimbul (damage +1) fights Bilbo again in the fierce round, and lowered to 5 by
		// hand, so that he beats Bilbo's 3 without overwhelming him; the Runner raised to 10 by hand, so that it
		// beats Thorin's 7.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, fimbul, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, fimbul, null, -2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, fimbul}, new PhysicalCardImpl[]{thorin, runner});

		// The Ring not worn: Bilbo, 3, loses to Fimbul and takes two wounds, no doubt.
		scn.FreepsResolveSkirmish(bilbo);
		assertFalse(scn.RBWearingOneRing());
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetBurdens());

		// Put on in Thorin's skirmish (the text names no skirmish): Thorin's wound for losing is a wound all the same.
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(thorin));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetBurdens());

		// The fierce round, the Ring worn: Bilbo, 5, ties Fimbul, 5, and the same loss is two doubts and no wound.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, fimbul);
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(5, scn.GetStrength(bilbo));
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
