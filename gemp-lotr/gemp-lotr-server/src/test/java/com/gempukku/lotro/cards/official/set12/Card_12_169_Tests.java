package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_169_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The card is not unique: two copies, so that "this condition" can be told from another.
					put("gaze", "12_169");
					put("gaze2", "12_169");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl, strength 5; nothing below 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, no special ability
					put("gimli", "1_13");       // Gimli, Son of Glóin: "Skirmish: Exert Gimli to make him strength +2."
					put("sam", "1_311");        // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3; "Fellowship: Exert Elrond to draw a card."
					put("draught", "9_24");     // Ent Draught: a [gandalf] condition on a companion (cheated on: no Ent to spot);
					                            //   "Skirmish: Exert bearer to make him or her damage +1."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsGazeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sauron's Gaze
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>The Free Peoples player cannot use bearer's special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gaze");

		assertEquals("Sauron's Gaze", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishAgainstANazgulACopyInYourSupportAreaIsTransferredToTheCompanionFightingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var gaze2 = scn.GetShadowCard("gaze2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(gaze, gaze2);
		scn.MoveMinionsToTable(nelya, khamul, hound);

		assertInZone(Zone.SUPPORT, gaze);
		assertInZone(Zone.SUPPORT, gaze2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, hound},
				new PhysicalCardImpl[]{gimli, khamul});

		// First, Boromir against the Morgul Hound, a [wraith] minion who is no Nazgûl.  Aragorn and Gimli are assigned
		// to Nazgûl, but their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gaze));
		assertFalse(scn.ShadowActionAvailable(gaze2));
		scn.ShadowPass();

		// Now Aragorn against Nelya: each copy offers itself.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gaze));
		assertTrue(scn.ShadowActionAvailable(gaze2));

		scn.ShadowUseCardAction(gaze);

		// Aragorn is the one character skirmishing a Nazgûl, so nothing is asked; Gimli, still waiting for Khamûl,
		// is not given it.
		assertAttachedTo(gaze, aragorn);
		assertNotAttachedTo(gaze, gimli);
		assertInZone(Zone.SUPPORT, gaze2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy on Aragorn is no longer in your support area and offers nothing; the other still does.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(gaze));
		assertTrue(scn.ShadowActionAvailable(gaze2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingANazgulIsGivenIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var khamul = scn.GetShadowCard("khamul");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(gaze);
		assertInZone(Zone.SUPPORT, gaze);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, khamul);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gaze));

		scn.ShadowUseCardAction(gaze);

		assertAttachedTo(gaze, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnceItIsTransferredToHimTheFreePeoplesPlayerCannotUseGimlisSkirmishAbilityAndStillCanUseOneOfACardHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var nelya = scn.GetShadowCard("nelya");
		var gimli = scn.GetFreepsCard("gimli");
		var draught = scn.GetFreepsCard("draught");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, draught);

		scn.StartGame();
		scn.MoveCardsToSupportArea(gaze);
		scn.MoveMinionsToTable(nelya);

		assertAttachedTo(draught, gimli);
		assertInZone(Zone.SUPPORT, gaze);
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);

		// Before the transfer, Gimli's own ability and the Ent Draught's are both his to use.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(draught));
		scn.FreepsPass();

		scn.ShadowUseCardAction(gaze);
		assertAttachedTo(gaze, gimli);

		// Gimli's own special ability is barred; the Ent Draught's, a card he bears, is not his and is still offered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(draught));
		assertEquals(0, scn.GetWoundsOn(gimli));
	}

	@Test
	public void AnAllyBearingItCannotUseHisAbilityWhileACompanionWhoDoesNotBearItStillCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gaze = scn.GetShadowCard("gaze");
		var elrond = scn.GetFreepsCard("elrond");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.FreepsActionAvailable(sam));

		// Elrond took it in a skirmish against a Nazgûl at his home site, and still bears it on a later turn.
		scn.AttachCardsTo(elrond, gaze);
		scn.FreepsRefreshDecision();

		assertAttachedTo(gaze, elrond);
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.FreepsActionAvailable(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
