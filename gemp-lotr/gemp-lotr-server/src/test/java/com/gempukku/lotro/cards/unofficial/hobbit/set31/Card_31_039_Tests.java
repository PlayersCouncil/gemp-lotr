package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_039_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 (strength 3, vitality 4) as the
	// Ring-bearer; his skirmish ability, played on a [dwarven] skirmish event from hand, is not used. The
	// skirmishes of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("invisible", "31_39");
					// The One Ring of the Hobbit Draft Game, borne by Bilbo: "Skirmish: Add a doubt to wear The One
					// Ring until the regroup phase", and while worn Bilbo is strength +2
					put("ring", VirtualTableScenario.HobbitRing);
					// Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4; a fellowship ability
					put("thorin", "30_19");

					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
					// Wicked Spider: a fierce [spider] Spider, 7/2; a skirmish ability the Shadow player does not use
					put("spider", "31_55");
					// Dark Bats: [moria] Creature, 4/2; a Shadow ability not used
					put("bats", "32_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AnInvisibleRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: An Invisible Ring
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If Bilbo wears the One Ring, wound each minion he is skirmishing. Then remove this event from the game.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("invisible");

		assertEquals("An Invisible Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OnlyWhileBilboWearsTheRingItWoundsEachMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invisible = scn.GetFreepsCard("invisible");
		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, ring);
		scn.MoveCardsToHand(invisible);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		assertInHand(invisible);
		scn.MoveMinionsToTable(runner);
		scn.MoveMinionsToTable(spider);
		scn.MoveMinionsToTable(bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner}, new PhysicalCardImpl[]{thorin, bats});
		// The Spider is left over, and the Shadow player puts it on Bilbo as well.
		scn.ShadowAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);

		// The Ring is on Bilbo but not worn: the event cannot be played.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.RBWearingOneRing());
		assertFalse(scn.FreepsPlayAvailable(invisible));

		assertEquals(0, scn.GetBurdens());
		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		assertEquals(1, scn.GetBurdens());
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(spider));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.FreepsPlayAvailable(invisible));
		scn.FreepsPlayCard(invisible);

		// Both minions skirmishing Bilbo are wounded, the Runner with its one vitality killed; the Bats, skirmishing
		// Thorin, are not.
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(spider));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WornInAnotherCompanionsSkirmishItIsPlayedAndWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invisible = scn.GetFreepsCard("invisible");
		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, ring);
		scn.MoveCardsToHand(invisible);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		assertInHand(invisible);
		scn.MoveMinionsToTable(runner);
		scn.MoveMinionsToTable(bats);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner}, new PhysicalCardImpl[]{thorin, bats});
		// Thorin's skirmish first: Bilbo is assigned to the Runner and waits for his own.
		scn.FreepsResolveSkirmish(thorin);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertTrue(scn.FreepsPlayAvailable(invisible));
		scn.FreepsPlayCard(invisible);

		// Bilbo is skirmishing no minion yet: neither the Bats Thorin is fighting nor the Runner waiting for Bilbo
		// is wounded.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));
		assertInZone(Zone.SHADOW_CHARACTERS, runner, bats);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ThenTheEventIsRemovedFromTheGameAndNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var invisible = scn.GetFreepsCard("invisible");
		var ring = scn.GetFreepsCard("ring");
		var bilbo = scn.GetRingBearer();
		var spider = scn.GetShadowCard("spider");
		scn.AttachCardsTo(bilbo, ring);
		scn.MoveCardsToHand(invisible);

		scn.StartGame();
		assertAttachedTo(ring, bilbo);
		assertInHand(invisible);
		scn.MoveMinionsToTable(spider);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);

		scn.FreepsUseCardAction(ring);
		assertTrue(scn.RBWearingOneRing());
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(spider));
		scn.FreepsPlayCard(invisible);

		assertEquals(1, scn.GetWoundsOn(spider));
		assertInZone(Zone.REMOVED, invisible);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
