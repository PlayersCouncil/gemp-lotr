package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_113_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("countryside", "53_113");

					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; Fellowship: exert Sam to remove a burden
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion with no race, Ring-bound; to play, add a burden (cheated in); an assignment ability only

					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event, spot an Elf to remove a burden
					put("salt", "11_171");      // Salt from the Shire: [shire] possession on a Hobbit; Fellowship: discard it to remove a burden
					put("impatient", "4_307");  // Impatient and Angry: [shire] Skirmish event, exert Sam for +3; if he wins, may remove a burden

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheShireCountrysideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: The Shire Countryside
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [Shire] companions. Plays to your support area.
		 * Each time you remove a burden (except by a Hobbit's game text), you may heal up to 2 companions
		 * (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("countryside");

		assertEquals("The Shire Countryside", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoShireCompanionsCanBeSpottedThenPlaysForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(countryside, sam);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();

		// Frodo is one [shire] companion; Bilbo is [shire] but an ally, Aragorn a companion but [gondor].
		assertFalse(scn.FreepsPlayAvailable(countryside));

		scn.FreepsPlayCard(sam);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(countryside));

		scn.FreepsPlayCard(countryside);
		// 2 for Sam + 1
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, countryside);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireCompanionWhoIsNoHobbitCountsTowardTheTwoSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(countryside);
		// Smeagol is a [shire] companion but no Hobbit: with Frodo, two [shire] companions and one Hobbit.
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(countryside));
		scn.FreepsPlayCard(countryside);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, countryside);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovingABurdenMayHealUpToTwoCompanionsOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var elfsong = scn.GetFreepsCard("elfsong");
		var salt = scn.GetFreepsCard("salt");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(sam, legolas, aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(frodo, salt);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(troop, 1);

		assertEquals(2, scn.GetBurdens());
		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		// Companions of every kind are offered; a wounded ally and a wounded minion are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, legolas, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, troop));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(sam, aragorn);

		// One wound healed from each of the two chosen, who carried two.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second burden removed in the same phase heals nothing.
		scn.FreepsUseCardAction(salt);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenRemovedByAHobbitsGameTextHealsNothingButOneRemovedByACardHeBearsMay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var salt = scn.GetFreepsCard("salt");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(frodo, salt);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 1);

		// Sam's own ability: a Hobbit's game text.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Salt from the Shire is a [shire] card borne by a Hobbit, but its game text is not the Hobbit's.
		scn.FreepsUseCardAction(salt);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheLimitIsOncePerPhaseSoABurdenRemovedInTheSkirmishPhaseHealsAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var sam = scn.GetFreepsCard("sam");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfsong = scn.GetFreepsCard("elfsong");
		var impatient = scn.GetFreepsCard("impatient");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(sam, legolas, aragorn);
		scn.MoveCardsToHand(elfsong, impatient);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(legolas, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(legolas);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage);
		scn.FreepsResolveSkirmish(sam);

		// Sam, strength 3 + 3, beats the strength 5 Uruk Savage (which survives Legolas's archery wound).
		scn.FreepsPlayCard(impatient);
		assertEquals(1, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(impatient));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCards(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenNoCompanionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var legolas = scn.GetFreepsCard("legolas");
		var bilbo = scn.GetFreepsCard("bilbo");
		var elfsong = scn.GetFreepsCard("elfsong");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(1);
		// Only an ally and a minion are wounded: no companion could be healed.
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(troop, 1);

		scn.FreepsPlayCard(elfsong);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
