package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_207_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_207");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_207");
					put("breath2", "1_207");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless his strength is tripled
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, +3 while skirmishing a Nazgûl
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_207");
					put("breath2", "1_207");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: no ability
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: spotted for Master of Healing; his own heal reaches allies only
					// Master of Healing: [elven] Fellowship or Regroup event, "Spot Elrond (or 2 [elven] allies) to
					// heal a companion or ally"
					put("healing1", "3_22");
					put("healing2", "3_22");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTransferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_207");
					// Strange-looking Men: "Maneuver: Exert this minion to transfer a possession or condition borne by
					// a character to another eligible bearer."
					put("men", "11_100");
					put("nelya", "2_84");       // Úlairë Nelya: a Nazgûl in play, so that only the phase keeps anyone from skirmishing one
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a fellowship ability and a response only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength +2 while he bears a weapon
					put("sword1", "1_299");     // Hobbit Sword: "Bearer must be a Hobbit", a hand weapon with no other text
					put("sword2", "1_299");
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
					put("breath", "1_207");
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackBreathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Black Breath
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl. Burdens and wounds may not be removed from bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breath");

		assertEquals("Black Breath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoBurdenIsRemovedWhileItIsOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(frodo, breath);

		scn.StartGame();
		scn.AddBurdens(2);

		assertAttachedTo(breath, frodo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		// An ability with a cost is offered though its effect will do nothing.
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenIsRemovedWhileItIsOnACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var breath = scn.GetShadowCard("breath");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		// The burdens are the Ring-bearer's, and Sam is not he: on Sam it keeps no burden where it is.  Nor
		// does it stop him exerting, which adds a wound and removes none.
		scn.AttachCardsTo(sam, breath);

		scn.StartGame();
		scn.AddBurdens(2);

		assertAttachedTo(breath, sam);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(sam));

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InASkirmishAgainstANazgulACopyInYourSupportAreaIsTransferredToTheCompanionFightingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var breath = scn.GetShadowCard("breath");
		var breath2 = scn.GetShadowCard("breath2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);

		scn.StartGame();
		scn.MoveCardsToSupportArea(breath, breath2);
		scn.MoveMinionsToTable(nelya, khamul, creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, creeper},
				new PhysicalCardImpl[]{arwen, khamul});

		// First, Boromir against the Morgul Creeper, a [wraith] minion who is no Nazgûl.  Aragorn and Arwen are
		// assigned to Nazgûl, but their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(breath));
		assertFalse(scn.ShadowActionAvailable(breath2));
		scn.ShadowPass();

		// 8 against 7: Boromir loses.
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Now Aragorn against Nelya: each copy offers itself.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(breath));
		assertTrue(scn.ShadowActionAvailable(breath2));

		scn.ShadowUseCardAction(breath);

		// Aragorn is the one character skirmishing a Nazgûl, so nothing is asked; Arwen, still waiting for Khamûl,
		// is not given it.
		assertAttachedTo(breath, aragorn);
		assertNotAttachedTo(breath, arwen);
		assertInZone(Zone.SUPPORT, breath2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy on Aragorn is no longer in your support area and offers nothing; the other still does.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(breath));
		assertTrue(scn.ShadowActionAvailable(breath2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingANazgulIsGivenIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var breath = scn.GetShadowCard("breath");
		var khamul = scn.GetShadowCard("khamul");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(breath);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, khamul);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(breath));

		scn.ShadowUseCardAction(breath);

		assertAttachedTo(breath, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsAreNotHealedOnItsBearerAndAreOnOtherCharacters() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealScenario();

		var breath = scn.GetShadowCard("breath");
		var breath2 = scn.GetShadowCard("breath2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, healing2);

		scn.StartGame();
		scn.MoveCardsToSupportArea(breath);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(elrond, 1);

		// While it lies in the support area, Aragorn is healed like anyone else.
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, frodo, elrond));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A companion and an ally each bear a copy.
		scn.AttachCardsTo(aragorn, breath);
		scn.AttachCardsTo(elrond, breath2);
		scn.FreepsRefreshDecision();

		// Borne, it keeps their wounds on them; the other wounded characters may still be healed.
		scn.FreepsPlayCard(healing2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, elrond));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrangeLookingMenCannotTransferItInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var breath = scn.GetShadowCard("breath");
		var men = scn.GetShadowCard("men");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, sam, merry);
		scn.AttachCardsTo(sam, sword1);
		scn.AttachCardsTo(merry, sword2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, breath);
		scn.MoveMinionsToTable(men, nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(men));

		scn.ShadowUseCardAction(men);
		assertEquals(1, scn.GetWoundsOn(men));

		// Each Hobbit Sword may go to Frodo, who bears no hand weapon.  Black Breath may go only to a character
		// skirmishing a Nazgûl, and in the maneuver phase there is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword1, sword2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath));
		scn.ShadowChooseCard(sword1);

		assertAttachedTo(sword1, frodo);
		assertAttachedTo(breath, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
