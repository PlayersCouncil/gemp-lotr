package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_107_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aldred", "12_107");
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon possession on a [rohan] Man; damage +1 only against an Uruk-hai
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; "Valiant." is his whole text

					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10; fierce only at 2 burdens or 2 wounds on Frodo
					put("steed", "1_208");      // Black Steed: [wraith] mount POSSESSION on a Nazgûl (strength +2 at a plains)
					put("ring", "9_42");        // Ring of Asperity: [wraith] ARTIFACT on a Nazgûl; its play trigger is not reached (cheated on)
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 is its whole text
					put("banner", "6_59");      // Banner of Isengard: [isengard] hand weapon possession; its win trigger exerts once for each site the Shadow player controls, and he controls none
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its play trigger is not reached (cheated in)
					put("spear", "1_182");      // Goblin Spear: [moria] hand weapon possession; answers a kill by its bearer only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, fierce; its other text is about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AldredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Aldred, Éored Soldier
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: At the start of each skirmish involving Aldred, you may discard a possession borne by a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aldred");

		assertEquals("Aldred", card.getBlueprint().getTitle());
		assertEquals("Éored Soldier", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfHisSkirmishHeMayDiscardAPossessionOfAMinionHeIsSkirmishingAndNoOtherSkirmishOffersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldred = scn.GetFreepsCard("aldred");
		var sword = scn.GetFreepsCard("sword");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aldred, guard);
		scn.AttachCardsTo(aldred, sword);

		var nelya = scn.GetShadowCard("nelya");
		var steed = scn.GetShadowCard("steed");
		var ring = scn.GetShadowCard("ring");
		var savage = scn.GetShadowCard("savage");
		var banner = scn.GetShadowCard("banner");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		assertEquals(aldred, sword.getAttachedTo());

		scn.MoveMinionsToTable(nelya, savage, runner);
		scn.AttachCardsTo(nelya, steed, ring);
		scn.AttachCardsTo(savage, banner);
		scn.AttachCardsTo(runner, spear);
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(nelya, ring.getAttachedTo());
		assertEquals(savage, banner.getAttachedTo());
		assertEquals(runner, spear.getAttachedTo());
		assertInZone(Zone.ATTACHED, steed, ring, banner, spear);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aldred, nelya}, new PhysicalCardImpl[]{guard, runner});
		// The Savage, left over, is the Shadow player's to assign: a second minion skirmishing Aldred.
		scn.ShadowAssignToMinions(aldred, savage);

		// The Guard's skirmish first: Aldred's minions are assigned to him and waiting, and nothing is offered.
		scn.FreepsResolveSkirmish(guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aldred));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Runner, strength 7 with the Spear, beats the Guard (6), who takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsResolveSkirmish(aldred);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aldred));
		scn.FreepsAcceptOptionalTrigger();

		// A possession of either minion skirmishing him; not the artifact, not his own Sword, not the Spear of
		// the Runner, whose skirmish is over.
		assertTrue(scn.FreepsHasCardChoiceAvailable(steed, banner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, sword, spear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(banner);

		assertInZone(Zone.DISCARD, banner);
		assertEquals(nelya, steed.getAttachedTo());
		assertEquals(nelya, ring.getAttachedTo());
		assertEquals(aldred, sword.getAttachedTo());
		assertEquals(runner, spear.getAttachedTo());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoPossessionOnAMinionHeIsSkirmishingNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldred = scn.GetFreepsCard("aldred");
		var sword = scn.GetFreepsCard("sword");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aldred, guard);
		scn.AttachCardsTo(aldred, sword);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		assertEquals(aldred, sword.getAttachedTo());

		scn.MoveMinionsToTable(troop, runner);
		scn.AttachCardsTo(runner, spear);
		assertEquals(runner, spear.getAttachedTo());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aldred, troop}, new PhysicalCardImpl[]{guard, runner});
		scn.FreepsResolveSkirmish(aldred);

		// The Troop bears nothing; the Spear is on a minion assigned to the Guard, and the Sword is Aldred's own.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aldred));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(runner, spear.getAttachedTo());
		assertEquals(aldred, sword.getAttachedTo());
	}

	@Test
	public void DeclinedAtTheFirstSkirmishItIsOfferedAgainAtTheStartOfTheFierceOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aldred = scn.GetFreepsCard("aldred");
		scn.MoveCompanionsToTable(aldred);

		var pursuer = scn.GetShadowCard("pursuer");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.AttachCardsTo(pursuer, banner);
		assertEquals(pursuer, banner.getAttachedTo());
		assertInZone(Zone.ATTACHED, banner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aldred, pursuer);
		scn.FreepsResolveSkirmish(aldred);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aldred));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(pursuer, banner.getAttachedTo());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Pursuer, 5 and 1 for the Banner, beats Aldred (5): one wound, and both live.  The Banner's win
		// trigger (an exertion for each site the Shadow player controls: none) is ordered with the rule's wound.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aldred));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aldred, pursuer);
		scn.FreepsResolveSkirmish(aldred);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aldred));
		scn.FreepsAcceptOptionalTrigger();
		// The Banner is the one candidate, and goes unasked.
		assertInZone(Zone.DISCARD, banner);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
