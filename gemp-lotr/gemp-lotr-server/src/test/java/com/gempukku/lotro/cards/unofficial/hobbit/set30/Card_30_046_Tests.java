package com.gempukku.lotro.cards.unofficial.hobbit.set30;

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

public class Card_30_046_Tests
{

	// The Hobbit table: the Hobbit path, and this Bilbo as the Ring-bearer (the table's default one), with no
	// card in the Ring slot.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; an assignment ability that wants threats or a companion of cost 0 who is not the Ring-bearer
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum] minion who is no Gollum, strength 8, fierce, enduring
					put("smaug", "30_66");          // Smaug, The Golden: [smaug] minion, strength 17, damage +2, fierce; discarded if not at a mountain
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; a text on being played, never reached
					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a fellowship ability only
					put("battlefury", "30_3");      // Battle Fury: [dwarven] skirmish event, "Make a Dwarf character strength +3 (or +4 if he bears a [dwarven] follower)"
					put("noble", "30_14");          // Noble intentions: [dwarven] skirmish event, "Exert a companion (except Bilbo) to make Bilbo strength +3"
					put("tome", "30_20");           // To me! O my kinsfolk!: [dwarven] FELLOWSHIP event
					put("shirenoble", "1_304");     // Noble Intentions: [shire] skirmish event, "Exert a companion (except a Hobbit) to make a Hobbit strength +3"
				}},
				null,
				VirtualTableScenario.BilboRB,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bilbo, Reliable Companion
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Burglar</b>.
		 * 	Bilbo cannot be overwhelmed by Smaug or Gollum unless his strength is tripled.
		 * 	<b>Skirmish:</b> Play a [dwarven] skirmish event to draw 2 cards (or 4 cards if Bilbo is skirmishing).
		 */

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("30_46", card.getBlueprintId());
		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Reliable Companion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BURGLAR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void GollumAtDoubleBilbosStrengthWoundsHimAndDoesNotOverwhelmHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Gollum 5 + 1 = 6: double Bilbo's 3, which overwhelms anyone else.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gollum, null, 1));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.PassSkirmishActions();

		assertInZone(Zone.FREE_CHARACTERS, bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GollumHalfAWraithHimselfShortOfTripleWoundsBilboAndDoesNotOverwhelmHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("wraithgollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Gollum 5 + 3 = 8: more than double Bilbo's 3, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gollum, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(8, scn.GetStrength(gollum));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.PassSkirmishActions();

		assertInZone(Zone.FREE_CHARACTERS, bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GollumAtTripleBilbosStrengthOverwhelmsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Gollum 5 + 4 = 9: triple Bilbo's 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gollum, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(9, scn.GetStrength(gollum));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.PassSkirmishActions();

		// Overwhelmed, the Ring-bearer is killed whatever his vitality, and the game is over.
		assertInZone(Zone.DEAD, bilbo);
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void SmaugShortOfTripleBilbosStrengthWoundsHimAndDoesNotOverwhelmHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var smaug = scn.GetShadowCard("smaug");

		// Erebor, site 7, is a mountain: Smaug stays there (he is discarded anywhere else, so he is laid there).
		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);
		// Bilbo 3 + 3 = 6 against Smaug's 17: more than double, one short of triple (18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(6, scn.GetStrength(bilbo));
		assertEquals(17, scn.GetStrength(smaug));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.PassSkirmishActions();

		// A wound and Smaug's damage +2, on vitality 4.
		assertInZone(Zone.FREE_CHARACTERS, bilbo);
		assertEquals(3, scn.GetWoundsOn(bilbo));
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ShelobAtDoubleOverwhelmsBilboAndGollumAtDoubleOverwhelmsAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		// Gollum 5 + 9 = 14: double Balin's 7.  Shelob's 8 is more than double Bilbo's 3 and short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gollum, null, 9));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, gollum}, new PhysicalCardImpl[]{bilbo, shelob});

		scn.FreepsResolveSkirmish(balin);
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(14, scn.GetStrength(gollum));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, balin);
		assertInZone(Zone.FREE_CHARACTERS, bilbo);

		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(8, scn.GetStrength(shelob));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, bilbo);
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void SkirmishActionPlaysADwarvenSkirmishEventToDraw4CardsWhileBilboIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var battlefury = scn.GetFreepsCard("battlefury");
		var noble = scn.GetFreepsCard("noble");
		var tome = scn.GetFreepsCard("tome");
		var shirenoble = scn.GetFreepsCard("shirenoble");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(battlefury, noble, tome, shirenoble);

		scn.StartGame();
		assertInZone(Zone.HAND, battlefury, noble, tome, shirenoble);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(4, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(3, scn.GetStrength(bilbo));
		assertEquals(0, scn.GetWoundsOn(balin));
		// The [shire] skirmish event can be played here: only its culture keeps it out of the choice below.
		assertTrue(scn.FreepsPlayAvailable(shirenoble));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);

		assertTrue(scn.FreepsHasCardChoiceAvailable(battlefury, noble));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tome, shirenoble));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(noble);

		// Noble intentions is played: its own cost exerts Balin, its effect makes Bilbo strength +3.
		assertInZone(Zone.DISCARD, noble);
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(6, scn.GetStrength(bilbo));
		// 4 in hand - the event + 4 drawn
		assertEquals(7, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertInZone(Zone.HAND, battlefury, tome, shirenoble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionDrawsOnly2CardsWhileBilboWaitsForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var battlefury = scn.GetFreepsCard("battlefury");
		var tome = scn.GetFreepsCard("tome");
		var runner = scn.GetShadowCard("runner");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(battlefury, tome);

		scn.StartGame();
		assertInZone(Zone.HAND, battlefury, tome);
		scn.MoveMinionsToTable(runner, gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, gollum});
		// Balin's skirmish first: Bilbo is assigned and not yet skirmishing.
		scn.FreepsResolveSkirmish(balin);

		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(7, scn.GetStrength(balin));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);

		// Battle Fury: Balin is the one Dwarf character, and bears no follower.
		assertInZone(Zone.DISCARD, battlefury);
		assertEquals(10, scn.GetStrength(balin));
		// 2 in hand - the event + 2 drawn
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWithoutADwarvenSkirmishEventInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var tome = scn.GetFreepsCard("tome");
		var shirenoble = scn.GetFreepsCard("shirenoble");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(tome, shirenoble);

		scn.StartGame();
		assertInZone(Zone.HAND, tome, shirenoble);
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);

		assertInZone(Zone.HAND, tome, shirenoble);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(shirenoble));
		assertFalse(scn.FreepsActionAvailable(bilbo));
	}
}
