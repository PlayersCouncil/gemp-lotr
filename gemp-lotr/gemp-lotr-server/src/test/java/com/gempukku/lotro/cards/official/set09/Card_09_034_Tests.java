package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("narsil", "9_34");
					// [gondor] Men.  Elendil's and Isildur's own texts do nothing on these boards (Elendil's two
					// abilities are used only on purpose; Isildur's are for him as Ring-bearer, or for knights).
					put("elendil", "9_32");     // Elendil, The Tall: companion, strength 8, vitality 5
					put("isildur", "9_33");     // Isildur, Bearer of Heirlooms: companion, strength 7, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion, strength 7, vitality 3
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, a Man, vitality 1; an ability used only on purpose
					// Ranger's Cloak: a [gondor] cloak, vitality +1, "Bearer must be a [gondor] Man": it gives the Merchant the
					// 2 vitality he needs to exert once.  Its own abilities are used only on purpose.
					put("cloak", "15_68");
					// The nearest characters that are not [gondor] Men.
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, valiant, no other text

					// [gondor] artifacts.  None of their abilities is used, and nothing in their texts acts on these boards.
					put("scroll", "9_36");      // Scroll of Isildur: support area
					put("shards", "3_44");      // The Shards of Narsil: support area
					put("anor", "9_37");        // Seeing Stone of Minas Anor: support area
					put("orthanc", "9_38");     // Seeing Stone of Orthanc: support area
					put("crown", "18_41");      // Crown of Gondor: support area; cost 4, to play spot Aragorn, Elendil or Isildur
					put("anduril", "7_80");     // Andúril, King's Blade: a hand weapon on Aragorn, defender +1 while no threat

					// The nearest cards that are not [gondor] artifacts.
					put("coat", "1_101");       // Coat of Mail: a [gondor] possession (armor) on a Man
					put("library", "101_11");   // Library of Rivendell: an [elven] artifact in the support area

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NarsilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Narsil, Blade of the Faithful
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Vitality: +1
		 * Game Text: Bearer must be a [gondor] Man.
		 * If bearer is Elendil or Isildur, he is strength +1 for each [gondor] artifact you can spot (limit +6).
		 * <b>Response:</b> If bearer is about to take a wound, exert 2 [gondor] Men to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("narsil");

		assertEquals("Narsil", card.getBlueprint().getTitle());
		assertEquals("Blade of the Faithful", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAGondorManAndMakesElendilStrengthPlusOneForEachGondorArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var scroll = scn.GetFreepsCard("scroll");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(elendil, isildur, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		// A [gondor] possession and an [elven] artifact, on the table throughout.
		scn.AttachCardsTo(elendil, scn.GetFreepsCard("coat"));
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("library"));
		scn.MoveCardsToHand(narsil, scroll);

		scn.StartGame();

		assertEquals(8, scn.GetStrength(elendil));
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.FreepsPlayAvailable(narsil));
		scn.FreepsPlayCard(narsil);

		// A [gondor] Man companion and a [gondor] Man ally are offered; a [gondor] Hobbit, a [rohan] Man and the
		// Ring-bearer are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(elendil, isildur, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, scn.GetRingBearer()));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(elendil);

		assertAttachedTo(narsil, elendil);
		// Narsil prints no strength: 8 + 1 for the one [gondor] artifact, Narsil itself.  Coat of Mail and the
		// Library are not counted; Isildur, who does not bear Narsil, is unchanged.
		assertEquals(9, scn.GetStrength(elendil));
		assertEquals(7, scn.GetStrength(isildur));

		// A second [gondor] artifact, played in the support area.
		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertEquals(10, scn.GetStrength(elendil));
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByIsildurHeIsStrengthPlusOneForEachGondorArtifactBorneOrInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var anduril = scn.GetFreepsCard("anduril");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(elendil, isildur, aragorn);
		scn.AttachCardsTo(isildur, narsil);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("shards"));

		scn.StartGame();

		// 7 + 2: Narsil and The Shards of Narsil.  Elendil does not bear Narsil and is unchanged.
		assertEquals(9, scn.GetStrength(isildur));
		assertEquals(8, scn.GetStrength(elendil));

		// A [gondor] artifact borne by another companion counts as well.
		scn.AttachCardsTo(aragorn, anduril);
		assertEquals(10, scn.GetStrength(isildur));
		assertEquals(8, scn.GetStrength(elendil));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherGondorManNarsilStrengthensNeitherHimNorElendilNorIsildur() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var elendil = scn.GetFreepsCard("elendil");
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(elendil, isildur, aragorn);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("scroll"));

		scn.StartGame();

		// Two [gondor] artifacts in play, and the bearer of Narsil is neither Elendil nor Isildur.
		assertAttachedTo(narsil, aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(elendil));
		assertEquals(7, scn.GetStrength(isildur));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlusSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var crown = scn.GetFreepsCard("crown");
		var elendil = scn.GetFreepsCard("elendil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(elendil, aragorn);
		scn.AttachCardsTo(elendil, narsil);
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("anduril"));
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("scroll"), scn.GetFreepsCard("shards"), scn.GetFreepsCard("anor"));
		scn.MoveCardsToHand(crown);

		scn.StartGame();

		// Five [gondor] artifacts: 8 + 5.
		assertEquals(13, scn.GetStrength(elendil));

		// Six: 8 + 6.
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("orthanc"));
		scn.FreepsRefreshDecision();
		assertEquals(14, scn.GetStrength(elendil));

		// A seventh, played: still 8 + 6.
		scn.FreepsPlayCard(crown);
		assertInZone(Zone.SUPPORT, crown);
		assertEquals(14, scn.GetStrength(elendil));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ResponseExertsTwoGondorMenToPreventAWoundToBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elendil = scn.GetFreepsCard("elendil");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir, elendil, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(merchant, scn.GetFreepsCard("cloak"));

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn (8) loses to the Troop (13) and is not overwhelmed.
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(narsil));
		scn.FreepsAcceptOptionalTrigger();

		// Two of the [gondor] Men exert, companions and an ally, the bearer among those offered; a [gondor] Hobbit,
		// a [rohan] Man and the Ring-bearer are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, elendil, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, scn.GetRingBearer()));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(boromir, merchant);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(elendil));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		// The wound is prevented.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherGondorManIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elendil = scn.GetFreepsCard("elendil");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir, elendil);
		scn.AttachCardsTo(aragorn, narsil);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		// Boromir (7) loses to the Troop (13) and is not overwhelmed.
		scn.PassCurrentPhaseActions();

		// Nothing is offered: the wound lands on Boromir, and the game goes on to the regroup phase.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(elendil));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneGondorManAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var narsil = scn.GetFreepsCard("narsil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, boromir, pippin, guard);
		scn.AttachCardsTo(aragorn, narsil);

		scn.StartGame();
		// Boromir, vitality 3, exhausted: Aragorn is the only [gondor] Man who can exert.  A [gondor] Hobbit and a
		// [rohan] Man could exert, and do not count.
		scn.AddWoundsToChar(boromir, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// Not offered: the wound lands on Aragorn, and the game goes on to the regroup phase.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
