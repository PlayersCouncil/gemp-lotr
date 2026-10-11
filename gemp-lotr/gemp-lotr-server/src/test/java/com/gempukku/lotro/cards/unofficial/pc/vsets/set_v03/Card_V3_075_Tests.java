package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_075_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "103_75");
					// Other Nazgul. None of their texts does anything on these boards.
					put("nertea", "1_234");     // Ulaire Nertea, Messenger of Dol Guldur: [wraith], strength 9, vitality 2, not fierce; text about being played
					put("attea", "103_61");     // Ulaire Attea, Khamul of the East: a [raider] Nazgul, fierce; his text needs 3 burdens
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: [wraith], not fierce; a Shadow ability
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith], twilight; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
					put("toldea", "12_180");    // Ulaire Toldea, Black Shadow: [wraith], fierce; his text is for when he is played
					put("cantea", "1_230");     // Ulaire Cantea, Lieutenant of Dol Guldur: [wraith], fierce; a skirmish ability of his own skirmishes
					// A [wraith] minion who is not a Nazgul; his text needs 3 wounds on the Ring-bearer.
					put("mongrel1", "7_194");   // Morgul Mongrel: [wraith] Orc, strength 7
					put("mongrel2", "7_194");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; Skirmish: exert Gimli to make him strength +2
					put("guard", "1_7");        // Dwarf Guard: strength 4; no text in play
					put("elf", "1_53");         // Lorien Elf: strength 4; no text in play
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3
					put("pippin", "1_306");     // Pippin, Friend to Frodo: strength 3; his text is about tales
					put("bounder", "1_286");    // Bounder: [shire] ally, home site 2
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, home site 3, strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireLemenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Lemenya, Anointed with Terror
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each character skirmishing this minion is strength -1 for each other Nazgul you can spot (or -1 for
		 * 	each other companion you can spot if you can spot 6 companions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lemenya");

		assertEquals("Ulaire Lemenya", card.getBlueprint().getTitle());
		assertEquals("Anointed with Terror", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForFiveAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 5 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.HAND, lemenya);
		assertTrue(scn.ShadowPlayAvailable(lemenya));

		scn.ShadowPlayCard(lemenya);

		// 8 - 5 to play - 2 roaming (a site 3 minion played at site 2)
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(10, scn.GetStrength(lemenya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionSkirmishingHerIsStrengthMinusOneForEachOtherNazgulAndFollowsTheirNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var elf = scn.GetFreepsCard("elf");
		var bounder = scn.GetFreepsCard("bounder");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var mongrel1 = scn.GetShadowCard("mongrel1");
		var mongrel2 = scn.GetShadowCard("mongrel2");
		// Five companions with Frodo, one short of six, and an ally beside them.
		scn.MoveCompanionsToTable(aragorn, gimli, guard, elf);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Two other Nazgul, one of them not a [wraith] card, and two [wraith] minions who are not Nazgul.
		scn.MoveMinionsToTable(lemenya, nertea, attea, mongrel1, mongrel2);
		scn.AddWoundsToChar(nertea, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, nertea});
		scn.ShadowDeclineAssignments();

		// Assigned to her, but his skirmish has not begun.
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		// 8 - 2 for Nertea and Attea
		assertEquals(6, scn.GetStrength(aragorn));
		// Nobody else: not Gimli, who waits to skirmish another Nazgul, not the Ring-bearer, not Lemenya herself.
		assertEquals(6, scn.GetStrength(gimli));
		// Frodo 3 + 1 for the Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(9, scn.GetStrength(nertea));

		scn.PassSkirmishActions();
		// Lemenya 10 beats Aragorn 6
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Gimli skirmishes Nertea while Lemenya is in play: his strength is his own.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		// 6 + 2 + 2 beats Nertea 9, whose second wound kills him.
		assertEquals(10, scn.GetStrength(gimli));
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, nertea);

		// The fierce skirmish: Attea is now the only other Nazgul.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		// 8 - 1 for Attea
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSixCompanionsItIsEachOtherCompanionInsteadHoweverManyNazgulAndNoLongerOnceOneDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var elf = scn.GetFreepsCard("elf");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var otsea = scn.GetShadowCard("otsea");
		var nelya = scn.GetShadowCard("nelya");
		var toldea = scn.GetShadowCard("toldea");
		var cantea = scn.GetShadowCard("cantea");
		// Six companions with Frodo, and an ally.
		scn.MoveCompanionsToTable(aragorn, gimli, guard, elf, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Six other Nazgul, each of another name: more of them than there are other companions.
		scn.MoveMinionsToTable(lemenya, nertea, attea, otsea, nelya, toldea, cantea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsResolveSkirmish(gimli);

		// 6 - 5 for the five other companions: not 6 for the Nazgul, and not both.
		assertEquals(1, scn.GetStrength(gimli));

		scn.PassSkirmishActions();
		// Lemenya 10 overwhelms Gimli 1: five companions are left, and the ally.
		assertInZone(Zone.DEAD, gimli);

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		// Five companions: 8 - 6 for the six other Nazgul, where the four other companions would make it 4.
		assertEquals(2, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSevenCompanionsACompanionSkirmishingHerIsStrengthMinusSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var elf = scn.GetFreepsCard("elf");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		// Seven companions with Frodo.
		scn.MoveCompanionsToTable(aragorn, gimli, guard, elf, sam, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nertea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsResolveSkirmish(aragorn);

		// 8 - 6 for the six companions other than Aragorn himself
		assertEquals(2, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingHerWithSixCompanionsIsStrengthMinusSixForEveryCompanionIsAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var elf = scn.GetFreepsCard("elf");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		// Six companions with Frodo.
		scn.MoveCompanionsToTable(aragorn, gimli, guard, elf, sam);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable(lemenya, attea);
		// The move is to site 3, Elrond's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(elrond));

		scn.FreepsResolveSkirmish(elrond);

		// 8 - 6: none of the six companions is the ally himself
		assertEquals(2, scn.GetStrength(elrond));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingHerWithFiveCompanionsIsStrengthMinusOneForEachOtherNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var elf = scn.GetFreepsCard("elf");
		var elrond = scn.GetFreepsCard("elrond");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var mongrel1 = scn.GetShadowCard("mongrel1");
		// Five companions with Frodo; with the ally, six Free Peoples characters.
		scn.MoveCompanionsToTable(aragorn, gimli, guard, elf);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(elrond);
		// One other Nazgul, a [raider] one, and a [wraith] minion who is not a Nazgul.
		scn.MoveMinionsToTable(lemenya, attea, mongrel1);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, lemenya);
		scn.ShadowDeclineAssignments();
		assertEquals(8, scn.GetStrength(elrond));

		scn.FreepsResolveSkirmish(elrond);

		// 8 - 1 for Attea
		assertEquals(7, scn.GetStrength(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
