package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("countryside", "3_113");

					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; Fellowship: exert Sam to remove a burden
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, vitality 3; an archery ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion with no race, Ring-bound; to play, add a burden (cheated in); an assignment ability only

					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event, spot an Elf to remove a burden
					put("salt", "11_171");      // Salt from the Shire: [shire] possession on a Hobbit; Fellowship: discard it to remove a burden

					// Ring of Rancor: [wraith] artifact on a Nazgul; Maneuver: remove a burden to heal each Nazgul
					put("rancor", "9_44");
					// Ulaire Lemenya: a Nazgul neither twilight nor fierce; strength -2 to bearers of [wraith] conditions only
					put("lemenya", "1_232");

					// Jarnsmid, Barding Emissary: a [gandalf] Man companion, no Hobbit; at the start of his skirmish he
					// may remove a burden (one, with no other [gandalf] companion)
					put("jarnsmid", "12_30");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("countryside", "3_113");
					// Elf-song: an [elven] Fellowship event of cost 1.  "Spot an Elf to remove a burden."
					put("elfsong", "1_39");
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: the Elf; an ally, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("merry", "1_303");      // Merry, From O'er the Brandywine: vitality 4
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
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [shire] companions. Plays to your support area.<br>Each time you remove a burden (except by a Hobbit's game text), you may heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("countryside");

		assertEquals("The Shire Countryside", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoShireCompanionsCanBeSpottedThenPlaysToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
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
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(countryside));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.FreepsPlayAvailable(countryside));

		scn.FreepsPlayCard(countryside);
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

		assertTrue(scn.FreepsPlayAvailable(countryside));
		scn.FreepsPlayCard(countryside);
		assertInZone(Zone.SUPPORT, countryside);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovingABurdenWithAnEventMayHealACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var elfsong = scn.GetFreepsCard("elfsong");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(merry, 1);
		// A wounded ally: no companion.
		scn.AddWoundsToChar(elrond, 1);

		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertTrue(scn.FreepsPlayAvailable(elfsong));
		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, elrond));

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenABurdenIsRemovedAndNoCompanionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var elfsong = scn.GetFreepsCard("elfsong");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.AddBurdens(2);
		// A wounded ally, and no wounded companion: the heal would do nothing.
		scn.AddWoundsToChar(elrond, 1);

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertInZone(Zone.SUPPORT, countryside);
		assertTrue(scn.FreepsPlayAvailable(elfsong));
		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(elrond));
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
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));

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
	public void ABurdenRemovedByANonHobbitCompanionsGameTextMayHealACompanionButNeverAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var jarnsmid = scn.GetFreepsCard("jarnsmid");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(jarnsmid, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(jarnsmid, 1);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(jarnsmid));
		assertEquals(1, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(jarnsmid, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(jarnsmid);

		// Jarnsmid's own text removes the burden: a [gandalf] Man's, so not a Hobbit's.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(jarnsmid));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, jarnsmid));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(jarnsmid));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachBurdenRemovedMayHealTheSameCompanionAgainInTheSamePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfsong = scn.GetFreepsCard("elfsong");
		var salt = scn.GetFreepsCard("salt");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(frodo, salt);
		scn.MoveCardsToHand(elfsong);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsPlayCard(elfsong);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second burden removed in the same phase: offered again, and the same companion healed again.
		scn.FreepsUseCardAction(salt);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(countryside));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenTheShadowPlayerRemovesHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var countryside = scn.GetFreepsCard("countryside");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(countryside);
		scn.MoveCompanionsToTable(aragorn);

		var rancor = scn.GetShadowCard("rancor");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, rancor);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(lemenya));

		// The Shadow player removes the burden, by a card that is no Hobbit: not "you".
		assertTrue(scn.ShadowActionAvailable(rancor));
		scn.ShadowUseCardAction(rancor);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
