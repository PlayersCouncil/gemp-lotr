package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "8_112");
					put("song2", "8_112");

					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit companion, Ring-bound; his abilities are used only on purpose
					// Smeagol, Once Like Us: a [shire] companion of no race, Ring-bound, vitality 4; his assignment ability
					// is used only on purpose
					put("smeagol", "103_116");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, strength 3, vitality 4; +2 only while he
					// bears a weapon, and he bears none here
					put("merry", "1_303");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, vitality 4; his trigger answers ANOTHER [rohan] Man
					// exerting, and his skirmish ability is used only on purpose
					put("rohanmerry", "103_85");
					// Pippin, Trollslayer: a [gondor] Hobbit companion; his skirmish ability is used only on purpose
					put("gondorpippin", "103_31");
					// Eowyn, Lady of Rohan: a [rohan] Man companion; her response answers a wound, never an exertion
					put("eowyn", "4_270");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] Ent companion who is no Gandalf; +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man; his maneuver ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; his skirmish ability is used only on purpose
					// Rohirrim Herdsman: a [rohan] ally; his regroup ability is used only on purpose
					put("herdsman", "7_247");
					// Barliman Butterbur: a [gandalf] ally; his fellowship ability is used only on purpose
					put("barliman", "1_70");

					put("brigand", "4_10");     // Dunlending Brigand: strength 8, no text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, damage +1
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6; stronger only against a bearer of a search card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SongoftheShireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Song of the Shire
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert a [shire] companion and either a [rohan] companion or a [gandalf] companion to add a [shire] token here.<br><b>Skirmish:</b> Remove a [shire] token here to prevent an unbound companion from being overwhelmed unless his or her strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("song");

		assertEquals("Song of the Shire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionExertsAShireAndARohanCompanionToAddATokenToThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var rohanmerry = scn.GetFreepsCard("rohanmerry");
		var gondorpippin = scn.GetFreepsCard("gondorpippin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var herdsman = scn.GetFreepsCard("herdsman");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(sam, smeagol, rohanmerry, gondorpippin, eowyn, gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(bounder, herdsman, barliman);
		scn.MoveCardsToSupportArea(song, song2);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetCultureTokensOn(song2));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(rohanmerry));

		assertTrue(scn.FreepsActionAvailable(song));
		scn.FreepsUseCardAction(song);

		// The [shire] companions, a Hobbit or not, the Ring-bearer among them.  Not the [shire] ally, nor a Hobbit
		// companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, gondorpippin, rohanmerry, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// Either a [rohan] companion (a Hobbit and a Man) or a [gandalf] one (two titles), all in one choice.  Not
		// the [rohan] or [gandalf] ally, a [gondor] companion, or a [shire] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rohanmerry, eowyn, gandalf, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herdsman, barliman, aragorn, frodo, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(rohanmerry);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(rohanmerry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// The token goes on the copy used, not on the other.
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetCultureTokensOn(song2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanCompanionAGandalfCompanionExertsAndTheShireCompanionNeedNotBeAHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(smeagol, avenger, aragorn);
		scn.MoveCardsToSupportArea(song);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(avenger));

		scn.FreepsUseCardAction(song);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		scn.FreepsChooseCard(smeagol);

		// The Ent Avenger is the one [rohan] or [gandalf] companion, so he exerts unasked; Aragorn does not.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionARohanCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToSupportArea(song);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(eowyn));

		// Frodo is the one [shire] companion and Eowyn the one [rohan] or [gandalf] companion: both exert unasked.
		scn.FreepsUseCardAction(song);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWhileNoShireCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var rohanmerry = scn.GetFreepsCard("rohanmerry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(rohanmerry, aragorn);
		scn.MoveCardsToSupportArea(song);
		// Vitality 4 with 3 wounds: the one [shire] companion is exhausted.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(rohanmerry));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(song));
	}

	@Test
	public void FellowshipActionIsNotOfferedWhileNoRohanOrGandalfCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var rohanmerry = scn.GetFreepsCard("rohanmerry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, rohanmerry, aragorn);
		scn.MoveCardsToSupportArea(song);
		// Vitality 4 with 3 wounds: the one [rohan] companion is exhausted.
		scn.AddWoundsToChar(rohanmerry, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(rohanmerry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(song));
	}

	@Test
	public void SkirmishActionRemovesATokenHereToKeepAnUnboundCompanionBeatenByDoubleFromBeingOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song, song2);
		scn.AddTokensToCard(song, 1);
		scn.AddTokensToCard(song2, 1);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetCultureTokensOn(song2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, brigand);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.FreepsActionAvailable(song));

		// Merry is the one unbound companion (the Ring-bearer is Ring-bound), so he is chosen unasked.
		scn.FreepsUseCardAction(song);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetCultureTokensOn(song2));
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));

		scn.ShadowPass();
		scn.FreepsPass();

		// 8 against 3 is double and less than triple: beaten and wounded, not overwhelmed.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTripleHisStrengthTheProtectedCompanionIsStillOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 1);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		assertEquals(1, scn.GetCultureTokensOn(song));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, raiders);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsUseCardAction(song);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(9, scn.GetStrength(raiders));

		scn.ShadowPass();
		scn.FreepsPass();

		// 9 against 3 is triple.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyUnboundCompanionMayBeChosenAndOnlyTheOneChosenIsProtected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 1);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertEquals(1, scn.GetCultureTokensOn(song));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, brigand);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsUseCardAction(song);
		// The unbound Hobbit skirmishing and an unbound Man of another culture in no skirmish.  Not the
		// Ring-bound Sam or Ring-bearer, the Hobbit ally, or the minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, bounder, brigand));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(3, scn.GetOverwhelmMultiplier(aragorn));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));

		scn.ShadowPass();
		scn.FreepsPass();

		// Aragorn was protected, not Merry: 8 against 3 overwhelms him.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoTokenOnThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var song2 = scn.GetFreepsCard("song2");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song, song2);
		scn.AddTokensToCard(song2, 1);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetCultureTokensOn(song2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, brigand);
		scn.FreepsResolveSkirmish(merry);

		// The token on the other copy pays for that copy only.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(song));
		assertTrue(scn.FreepsActionAvailable(song2));
	}

	@Test
	public void WithNoUnboundCompanionTheTokenIsRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 1);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertEquals(1, scn.GetCultureTokensOn(song));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand);
		scn.FreepsResolveSkirmish(sam);

		// The cost can be paid, so the action is offered (CR 4.2 "cost"); there is nobody to protect.
		assertTrue(scn.FreepsActionAvailable(song));
		scn.FreepsUseCardAction(song);

		assertEquals(0, scn.GetCultureTokensOn(song));
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheProtectionEndsWithTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(song);
		scn.AddTokensToCard(song, 1);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(seeker);
		assertEquals(1, scn.GetCultureTokensOn(song));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, seeker);
		scn.FreepsResolveSkirmish(merry);

		scn.FreepsUseCardAction(song);
		assertEquals(3, scn.GetOverwhelmMultiplier(merry));
		scn.ShadowPass();
		scn.FreepsPass();

		// 6 against 3: double, so protected he is only wounded.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// The fierce round is another skirmish phase: the same Merry against the same Seeker, unprotected.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, seeker);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassFierceSkirmishActions();

		// Still protected he would have a second wound and be alive.
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
