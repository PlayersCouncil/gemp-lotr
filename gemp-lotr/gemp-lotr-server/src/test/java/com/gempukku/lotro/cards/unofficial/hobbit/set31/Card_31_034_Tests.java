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

public class Card_31_034_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase
	// and the skirmishes of turn 1 are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarms", "31_34");
					put("swarms2", "31_34");

					put("footman", "30_42");    // Goblin Footman: [moria] Orc, cost 3, site 4, vitality 1; a "when you play" offer of a possession from the discard pile
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2, site 3, strength 6, vitality 2; an Assignment ability not used
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc, strength 7, vitality 2; a response at a battleground only
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, strength 4; a Shadow ability not used
					put("mace", "32_29");       // Azog's Mace: [moria] hand weapon, strength +2, on an Orc; a Shadow ability not used
					put("worms", "32_33");      // Were-worms: [moria] condition, cost 0; a skirmish-win trigger, a Shadow ability at a battleground
					put("warg", "32_32");       // Vicious Warg: [moria] mount on an Orc; makes him fierce

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, strength 7; a Maneuver ability not used
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinSwarmsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Goblin Swarms
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [moria] card.
		 * <b>Response:</b> If your Orc wins a skirmish, discard cards and wounds on that Orc and stack that Orc on this condition.
		 * <b>Shadow:</b> Play an Orc stacked here as if played from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swarms");

		assertEquals("Goblin Swarms", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMoriaCardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var aggressor = scn.GetShadowCard("aggressor");
		var worms = scn.GetShadowCard("worms");
		scn.MoveCardsToHand(swarms, worms);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.FreepsPass();

		// Only a [gundabad] Orc in play, and Were-worms in hand, which is not spotted there.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(swarms));

		// A [moria] condition played: a [moria] card that is neither a character nor an item.
		scn.ShadowPlayCard(worms);
		assertInZone(Zone.SUPPORT, worms);
		assertTrue(scn.ShadowPlayAvailable(swarms));
		scn.ShadowPlayCard(swarms);

		assertInZone(Zone.SUPPORT, swarms);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhileOnlyAMoriaPossessionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);
		scn.FreepsPass();

		// The [moria] mount on the [gundabad] Orc is the only [moria] card in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(swarms));
	}

	@Test
	public void CanBePlayedWhileOnlyAMoriaMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(swarms));
	}

	@Test
	public void WhenAnOrcWinsItsCardsGoToTheDiscardPileAndItIsStackedOnTheCopyWhoseResponseIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var swarms2 = scn.GetShadowCard("swarms2");
		var aggressor = scn.GetShadowCard("aggressor");
		var assaulting = scn.GetShadowCard("assaulting");
		var mace = scn.GetShadowCard("mace");
		var warg = scn.GetShadowCard("warg");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(swarms, swarms2);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, assaulting);
		// The Warg on the Aggressor, who leaves play; the Mace (no fierce) on the Goblin, who stays.
		scn.AttachCardsTo(aggressor, warg);
		scn.AttachCardsTo(assaulting, mace);
		scn.AddWoundsToChar(aggressor, 1);
		scn.AddWoundsToChar(assaulting, 1);
		assertAttachedTo(warg, aggressor);
		assertAttachedTo(mace, assaulting);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(assaulting));

		// The [gundabad] Aggressor fights Balin; the Assaulting Goblin, with a weapon and a wound of its own, stays
		// out of every skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// 6 + 3 against 7: the Aggressor wins, and each copy of Goblin Swarms offers its response.
		assertEquals(1, scn.GetWoundsOn(balin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms2));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertAttachedTo(warg, aggressor);

		scn.ShadowChooseOptionalTrigger(swarms2);
		assertInZone(Zone.STACKED, aggressor);
		assertTrue(scn.GetStackedCards(swarms2).contains(aggressor));
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.DISCARD, warg);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		// The Assaulting Goblin, not in the skirmish, keeps its weapon and its wound.
		assertAttachedTo(mace, assaulting);
		assertEquals(1, scn.GetWoundsOn(assaulting));

		// With the Aggressor gone, the other copy has nothing left to do and is not offered again.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(swarms));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnOrcLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// 6 against 7: the Aggressor loses and takes a wound, and Goblin Swarms is not offered.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertEquals(0, scn.GetStackedCards(swarms).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyAnOrcThatWonThatSkirmishCanBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var assaulting = scn.GetShadowCard("assaulting");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(balin, nori);
		scn.MoveCardsToSupportArea(swarms);

		scn.StartGame();
		scn.MoveMinionsToTable(footman, aggressor, bats, assaulting);

		// Balin takes the [gundabad] Aggressor, and the Shadow player gives him the [moria] Footman and the Bats too,
		// so that a [moria] Creature wins beside the two Orcs.  The Assaulting Goblin is a third Orc, assigned to
		// Nori, whose skirmish has not begun.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, aggressor}, new PhysicalCardImpl[]{nori, assaulting});
		scn.ShadowAssignToMinions(balin, footman, bats);
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// 6 + 8 + 4 against 7: Balin is overwhelmed and killed.
		assertInZone(Zone.DEAD, balin);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		scn.ShadowChooseOptionalTrigger(swarms);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, assaulting));
		scn.ShadowChooseCard(aggressor);

		assertTrue(scn.GetStackedCards(swarms).contains(aggressor));
		assertEquals(1, scn.GetStackedCards(swarms).size());
		assertInZone(Zone.SHADOW_CHARACTERS, footman, bats, assaulting);

		// The Footman won as well, and the response may be used again for it; declined, Nori's skirmish is next.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(swarms));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void ShadowAbilityPlaysAnOrcStackedHereAsIfFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var swarms2 = scn.GetShadowCard("swarms2");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var assaulting = scn.GetShadowCard("assaulting");
		scn.MoveCardsToSupportArea(swarms, swarms2);
		scn.StackCardsOn(swarms, footman, aggressor);
		scn.StackCardsOn(swarms2, assaulting);

		scn.StartGame();
		assertInZone(Zone.STACKED, footman, aggressor, assaulting);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.STACKED, footman, aggressor, assaulting);
		assertTrue(scn.ShadowActionAvailable(swarms));
		scn.ShadowUseCardAction(swarms);

		// Orcs of two cultures stacked here are offered; the Assaulting Goblin, on the other copy, is not.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(assaulting));
		scn.ShadowChooseCard(aggressor);

		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.STACKED, footman, assaulting);
		assertTrue(scn.GetStackedCards(swarms).contains(footman));
		assertTrue(scn.GetStackedCards(swarms2).contains(assaulting));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowAbilityIsNotOfferedUntilThePoolCanPayForAnOrcStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swarms = scn.GetShadowCard("swarms");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(swarms);
		scn.StackCardsOn(swarms, footman, aggressor);

		scn.StartGame();
		assertInZone(Zone.STACKED, footman, aggressor);
		scn.FreepsPass();

		// The Aggressor is the cheaper: 2 + 2 roaming at The Troll Hoard (site 2; its site is 3).
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.STACKED, footman, aggressor);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(swarms));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(swarms));
		scn.ShadowUseCardAction(swarms);

		// Only the Aggressor can be paid for, so it is played; the Footman (3 + 2) stays where it is.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.STACKED, footman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
