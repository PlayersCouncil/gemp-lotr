package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_027_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "17_27");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability only
					put("rohanaragorn", "17_93");   // Aragorn, Defender of Rohan: the one [rohan] Aragorn; hunter 1 only while the Ring-bearer is assigned
					put("knight", "5_35");          // Gondorian Knight: a [gondor] Man companion who is no Aragorn, strength 6; Knight is his whole text
					put("grimir", "1_17");          // Grimir, Dwarven Elder: an ally; a fellowship ability only
					// Weary: [isengard] condition on a companion or an ally; it answers an assignment to an [isengard] tracker
					put("weary", "4_212");
					put("weary2", "4_212");
					// No Stranger to the Shadows: [gondor] condition on a ranger (Aragorn); each site's Shadow number -1
					put("nostranger", "1_108");
					put("lord", "1_21");            // Lord of Moria: [dwarven] condition in the support area; each Dwarf damage +1
					put("sword", "1_299");          // Hobbit Sword: a possession on a Hobbit, strength +2, no other text
					// First Level: a [gondor] condition its own maneuver ability transfers to a minion; strength -1 there
					put("firstlevel", "7_92");

					put("raiders", "1_158");        // Uruk-hai Raiding Party: strength 9, vitality 3; Damage +1 is its whole text
					// White Hand Vanquisher: fierce, strength 8, vitality 3; damage +1 only for [uruk-hai] hunters assigned
					put("vanquisher", "17_134");
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Andúril, Sword That Was Broken
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be Aragorn. He is <b>damage +1</b>.<br>Each time Aragorn wins a skirmish, you may exert him to liberate a site or discard a condition borne by a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", card.getBlueprint().getTitle());
		assertEquals("Sword That Was Broken", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedWithoutAragornAndOnHimMakesHimDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		// A [gondor] Man companion who is no Aragorn, and Frodo: nobody may bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(anduril));

		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(anduril));

		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnTheRohanAragornAndMakesHimDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToHand(anduril);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(anduril));

		scn.FreepsPlayCard(anduril);
		assertAttachedTo(anduril, aragorn);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeAragornWinsHeMayExertToLiberateASiteOrDiscardAConditionBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var weary = scn.GetShadowCard("weary");
		var vanquisher = scn.GetShadowCard("vanquisher");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		scn.SkipToSite(2);
		// Player 1's second turn: each player has moved to site 2, so site 1 is behind every marker.
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(vanquisher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, vanquisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn 8 + 2 against the Vanquisher's 8: he wins, and the Vanquisher takes 1 wound and 1 for damage +1.
		assertEquals(2, scn.GetWoundsOn(vanquisher));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Discard a condition borne by a companion"));
		scn.FreepsChoose("Liberate a site");
		assertFalse(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertAttachedTo(weary, frodo);

		// The fierce round: Aragorn wins again and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, vanquisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(vanquisher);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));

		// No site is controlled now: discarding the condition is the only choice that can be carried out, and Weary
		// is the only condition borne by a companion, so nothing is asked.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertInDiscard(weary);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardsAConditionOfEitherSideBorneByACompanionAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var nostranger = scn.GetFreepsCard("nostranger");
		var lord = scn.GetFreepsCard("lord");
		var sword = scn.GetFreepsCard("sword");
		var firstlevel = scn.GetFreepsCard("firstlevel");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(aragorn, anduril, nostranger);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(lord);

		var weary = scn.GetShadowCard("weary");
		var weary2 = scn.GetShadowCard("weary2");
		var raiders = scn.GetShadowCard("raiders");
		scn.AttachCardsTo(frodo, weary);
		scn.AttachCardsTo(grimir, weary2);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.AttachCardsTo(raiders, firstlevel);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn 8 + 2 against the Raiding Party's 9 - 1: he wins, and it lives through its 2 wounds.
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));

		// Nobody controls a site on turn 1: the liberation cannot be carried out, so the discard is what is done.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// A Shadow condition on Frodo and a Free Peoples condition on Aragorn himself are offered; a condition in a
		// support area, one borne by an ally, one borne by a minion and a possession borne by a companion are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(weary, nostranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lord, weary2, firstlevel, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nostranger);

		assertInDiscard(nostranger);
		assertAttachedTo(weary, frodo);
		assertAttachedTo(sword, frodo);
		assertAttachedTo(weary2, grimir);
		assertAttachedTo(firstlevel, raiders);
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherCompanionWinningOffersNothingAndDecliningLeavesEverythingAsItWas() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, anduril);

		var weary = scn.GetShadowCard("weary");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{aragorn, raiders});

		// The Knight's 6 against the Savage's 5: the Knight wins.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(anduril));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornLosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var weary = scn.GetShadowCard("weary");
		var raiders = scn.GetShadowCard("raiders");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// The Raiding Party is 9 and Aragorn 8 + 2: with 3 more it beats him, but not by double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, raiders, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn loses: 1 wound and 1 for damage +1. He is in the skirmish that ends, but did not win it.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAragornIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var anduril = scn.GetFreepsCard("anduril");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		var weary = scn.GetShadowCard("weary");
		var raiders = scn.GetShadowCard("raiders");
		scn.AttachCardsTo(frodo, weary);

		scn.StartGame();
		// Vitality 4 + 1 with 4 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 4);
		scn.MoveMinionsToTable(raiders);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, raiders);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// He still wins, on strength.
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(4, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(anduril));
		assertAttachedTo(weary, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
