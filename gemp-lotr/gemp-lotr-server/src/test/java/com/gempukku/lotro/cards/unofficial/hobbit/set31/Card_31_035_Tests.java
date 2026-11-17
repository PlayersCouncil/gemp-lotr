package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_035_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase
	// and the skirmishes of turn 1 are at The Troll Hoard 30_50 (site 2: forest and underground, no battleground),
	// whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "31_35");
					put("power2", "31_35");     // a second copy: the card is not unique and says "this condition"

					put("footman", "30_42");    // Goblin Footman: [moria] Orc; a "when you play" offer, never reached (cheated in)
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6, vitality 2; an Assignment ability not used
					put("runner", "31_30");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a "when you play" offer, never reached (cheated in)
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, bearer for the Orkish Bow; a Regroup ability not used
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, strength 4, vitality 2; a Shadow ability not used
					put("balrog", "12_79");     // The Balrog: bearer for The Balrog's Sword; at an underground site he is fierce and takes no wounds
					put("worms", "32_33");      // Were-worms: [moria] condition, cost 0; a skirmish-win trigger, a Shadow ability at a battleground
					put("warg", "32_32");       // Vicious Warg: [moria] mount, cost 3, on an Orc; its (4) only at a battleground
					put("scimitar", "31_31");   // Goblin Scimitar: [moria] hand weapon (possession), cost 0, on an Orc; "you may draw a card"
					put("bow", "32_41");        // Orkish Bow: [sauron] ranged weapon (possession), cost 1, on a [sauron] Orc; its offer wants a [dwarven] weapon
					put("sword", "2_50");       // The Balrog's Sword: [moria] artifact hand weapon, cost 1, on The Balrog

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a Fellowship ability not used
					put("thorin", "30_19");     // Thorin, Oakenshield: bearer for Orcrist; strength +1 for each follower he bears, and he bears none
					put("orcrist", "30_17");    // Orcrist: [dwarven] artifact hand weapon, cost 2, on Thorin; a skirmish-win trigger never reached
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GreatGoblinsPowerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Great Goblin's Power
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a [moria] card.
		 * Each time you play a weapon, add (1).
		 * <b>Response:</b> If an Orc is about to take a wound, discard this condition or remove (3) to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("power");

		assertEquals("Great Goblin's Power", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAMoriaCardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var aggressor = scn.GetShadowCard("aggressor");
		var worms = scn.GetShadowCard("worms");
		scn.MoveCardsToHand(power, worms);

		scn.StartGame();
		assertInHand(power, worms);
		scn.MoveMinionsToTable(aggressor);
		scn.FreepsPass();

		// Only a [gundabad] Orc in play, and Were-worms in hand, which is not spotted there.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInHand(power, worms);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(power));

		// A [moria] condition played: a [moria] card that is neither a character nor an item.
		scn.ShadowPlayCard(worms);
		assertInZone(Zone.SUPPORT, worms);
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		assertInZone(Zone.SUPPORT, power);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhileOnlyAMoriaPossessionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);
		scn.FreepsPass();

		// The [moria] mount on the [gundabad] Orc is the only [moria] card in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(power));
	}

	@Test
	public void CanBePlayedWhileOnlyAMoriaMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(footman);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(power));
	}

	@Test
	public void AddsOneEachTimeItsPlayerPlaysAWeaponOfAnyCultureOrClass() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var balrog = scn.GetShadowCard("balrog");
		var scimitar = scn.GetShadowCard("scimitar");
		var bow = scn.GetShadowCard("bow");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToSupportArea(power);
		scn.MoveCardsToHand(scimitar, bow, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, veteran, balrog);
		assertInZone(Zone.SUPPORT, power);
		assertInHand(scimitar, bow, sword);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		// A [moria] hand weapon (possession): 0 to play, 1 added.
		scn.ShadowPlayCard(scimitar);
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(scimitar, aggressor);
		// The Scimitar's own "you may draw a card", declined.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(11, scn.GetTwilight());

		// A [sauron] ranged weapon (possession): 1 to play, 1 added.
		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, veteran);
		// The Bow's own offer is withheld: no [dwarven] weapon is in play to discard.
		assertEquals(11, scn.GetTwilight());

		// A [moria] artifact hand weapon: 1 to play, 1 added.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, balrog);
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsNothingForAPossessionThatIsNoWeaponOrAWeaponTheOtherPlayerPlays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");
		var thorin = scn.GetFreepsCard("thorin");
		var orcrist = scn.GetFreepsCard("orcrist");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToSupportArea(power);
		scn.MoveCardsToHand(orcrist, warg);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, power);
		assertInHand(orcrist, warg);
		assertEquals(0, scn.GetTwilight());

		// The Free Peoples player's hand weapon: 2 to play, nothing added.
		scn.FreepsPlayCard(orcrist);
		assertAttachedTo(orcrist, thorin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(aggressor);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		// A Shadow possession that is no weapon (a mount): 3 to play, nothing added.
		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, aggressor);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesThreeToPreventTheWoundOfAMoriaOrcWhenBothCostsCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var power2 = scn.GetShadowCard("power2");
		var runner = scn.GetShadowCard("runner");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(power, power2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertInZone(Zone.SUPPORT, power, power2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetWoundsOn(runner));

		// 7 against 5: the Runner (vitality 1) loses and is about to take a wound that would kill it.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(power));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(power2));
		scn.ShadowChooseOptionalTrigger(power);

		// With 3 in the pool both costs can be paid, and the Shadow player is asked which.
		assertEquals(List.of("Discard this condition", "Remove (3)"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove (3)");

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, power, power2);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheCopyOnlyAbleToDiscardItselfItIsDiscardedUnaskedToPreventTheWoundOfAGundabadOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var power2 = scn.GetShadowCard("power2");
		var aggressor = scn.GetShadowCard("aggressor");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(power, power2);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.SUPPORT, power, power2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(2);
		assertEquals(0, scn.GetWoundsOn(aggressor));

		// 7 against 6: the Aggressor loses.  Each copy offers its response.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(power));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(power2));
		scn.ShadowChooseOptionalTrigger(power2);

		// 2 in the pool cannot pay (3): the copy taken is discarded with nothing asked.
		assertInDiscard(power2);
		assertInZone(Zone.SUPPORT, power);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForTheWoundOfAMinionWhoIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(power);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		assertInZone(Zone.SUPPORT, power);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, bats);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(3);
		assertEquals(0, scn.GetWoundsOn(bats));

		// 7 against 4: the [moria] Creature loses and takes its wound, with both costs payable.
		scn.PassSkirmishActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(power));
		assertEquals(1, scn.GetWoundsOn(bats));
		assertInZone(Zone.SUPPORT, power);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PreventsTheWoundOfAnOrcChosenAmongTheLosersOfOneSkirmishAndNotOfAMinionWhoIsNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var aggressor = scn.GetShadowCard("aggressor");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(power);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, runner, bats);
		assertInZone(Zone.SUPPORT, power);
		// A strength only to beat three minions at once without doubling them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, balin, null, 9));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, aggressor);
		scn.ShadowAssignToMinions(balin, runner, bats);
		scn.FreepsResolveSkirmish(balin);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(bats));

		// 16 against 6 + 5 + 4: all three lose, and their wounds come together.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(power));
		scn.ShadowChooseOptionalTrigger(power);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		scn.ShadowChooseCard(runner);

		assertInDiscard(power);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(bats));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
