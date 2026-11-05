package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "4_118");
					// Faramir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3, Ring-bound and ranger, no
					// archer; his text only stops the opponent's skirmish actions in his skirmishes
					put("faramir", "4_117");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used
					// only on purpose
					put("boromir", "1_97");
					// Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Dunlending Footmen: [dunland] Man, site 3 (not roaming at site 3), strength 11, vitality 2; its
					// text is about its own cost
					put("footmen", "6_3");
					// Gollum, Stinker: minion of no race, site 3 (not roaming at site 3), strength 5 (+1 for each
					// burden: none here), vitality 4; his trigger needs him to win
					put("gollum", "5_25");
					// Goblin Patrol Troop: [moria] Orc, site 4 (roaming at site 3), strength 13, vitality 3, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirsBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Faramir's Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be Faramir.<br>He is an <b>archer</b>.<br><b>Skirmish:</b> If Faramir is skirmishing a Man or a roaming minion, exert Faramir to wound that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Faramir's Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnFaramirAloneAndMakesHimAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(faramir, boromir);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertFalse(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(bow);
		// Faramir is the one bearer the text allows, so nobody is asked: neither Boromir, another [gondor] Man
		// companion, nor Frodo could take it.
		assertAttachedTo(bow, faramir);
		assertTrue(scn.HasKeyword(faramir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boromir, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertInZone(Zone.HAND, bow);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bow));
	}

	@Test
	public void SkirmishAbilityExertsFaramirToWoundTheChosenManOrRoamingMinionHeIsSkirmishingAndNotTheOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var footmen = scn.GetShadowCard("footmen");
		var troop = scn.GetShadowCard("troop");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(footmen, troop, gollum);
		// The fellowship moves to site 3.
		scn.SkipToAssignments();
		// The bow makes Faramir an archer, so it goes on once the archery phase is behind.
		scn.AttachCardsTo(faramir, bow);
		scn.FreepsAssignToMinions(faramir, footmen);
		// The Troop and Gollum are left over, and the Shadow player puts them on Faramir as well.
		scn.ShadowAssignToMinions(faramir, troop, gollum);
		scn.FreepsResolveSkirmish(faramir);

		assertFalse(scn.HasKeyword(footmen, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		// The Footmen are a Man that is not roaming and the Troop a roaming minion that is no Man; Gollum, neither,
		// is no such minion.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(footmen, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gollum));
		scn.FreepsChooseCard(footmen);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(footmen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWoundsARoamingMinionWhoIsNoManAndIsNotOfferedBeforeFaramirsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var boromir = scn.GetFreepsCard("boromir");
		var footmen = scn.GetShadowCard("footmen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(faramir, boromir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(footmen, troop);
		scn.SkipToAssignments();
		scn.AttachCardsTo(faramir, bow);
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{faramir, troop},
				new PhysicalCardImpl[]{boromir, footmen}
		);
		scn.FreepsResolveSkirmish(boromir);

		// A Man is skirmishing, and a roaming minion waits for Faramir, but Faramir is skirmishing nobody yet.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(faramir);

		// Site number 4 at site 3.
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(troop));
		// The Man in the skirmish that is over is no minion Faramir is skirmishing.
		assertEquals(0, scn.GetWoundsOn(footmen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedAgainstAMinionThatIsNeitherAManNorRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.AttachCardsTo(faramir, bow);
		scn.FreepsAssignAndResolve(faramir, gollum);

		// Faramir could exert.
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileFaramirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var faramir = scn.GetFreepsCard("faramir");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.SkipToSite(2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(faramir, 2);
		scn.MoveMinionsToTable(footmen);
		scn.SkipToAssignments();
		scn.AttachCardsTo(faramir, bow);
		scn.FreepsAssignAndResolve(faramir, footmen);

		assertEquals(2, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
